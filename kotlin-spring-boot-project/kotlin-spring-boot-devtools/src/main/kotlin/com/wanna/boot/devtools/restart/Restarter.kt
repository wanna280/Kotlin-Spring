package com.wanna.boot.devtools.restart

import com.wanna.boot.devtools.restart.classloader.ClassLoaderFiles
import com.wanna.boot.devtools.restart.classloader.RestartClassLoader
import com.wanna.framework.beans.factory.ObjectFactory
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.ReflectionUtils
import com.wanna.framework.lang.Nullable
import com.wanna.boot.devtools.settings.DevToolsSettings
import com.wanna.common.logging.Logger
import com.wanna.common.logging.LoggerFactory
import java.beans.Introspector
import java.net.URL
import java.util.concurrent.Callable
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadFactory
import java.util.concurrent.locks.ReentrantLock
import kotlin.system.exitProcess

/**
 * SpringBoot Application的Restarter, 负责重启整个SpringBootApplication;
 * 一般使用单例对象去进行使用, 可以通过`Restarter.getInstance`获取到全局唯一的Restarter,
 * 这样就保证了不管是第几次去进行重启SpringApplication, 都是使用的同一个"Restarter"
 *
 * @param thread Restarter要使用的线程
 * @param mainClassName mainClassName
 * @param args 启动参数
 * @param applicationClassLoader ApplicationClassLoader
 * @param exceptionHandler ExceptionHandler
 * @param initialUrls initialUrls(有可能为null, 表示不需要启用DevTools), 维护了RestartClassLoader要去进行使用的URL列表
 */
open class Restarter(
    thread: Thread,
    private val mainClassName: String,
    private val args: Array<String>,
    private val applicationClassLoader: ClassLoader,
    private var exceptionHandler: Thread.UncaughtExceptionHandler,
    private var initialUrls: Array<URL>?
) {
    constructor(thread: Thread, args: Array<String>, initializer: RestartInitializer) : this(
        thread, getMainClassName(thread), args, thread.contextClassLoader,
        thread.uncaughtExceptionHandler, initializer.getInitialUrls(thread)
    )

    // 当前的Restarter当中需要去进行维护的RootContext列表, 使用COW去实现线程安全的访问
    private val rootContexts = CopyOnWriteArrayList<ConfigurableApplicationContext>()

    // Logger
    private var logger: Logger = LoggerFactory.getLogger(Restarter::class.java)

    // 是否启用了Restart功能? 如果设置为false, 则不会去进行重启
    var enabled = true

    // Restarter的属性信息
    private val attributes = HashMap<String, Any>()

    // RestarterClassLoader要去进行加载的URL列表(在初始化时, 会从initialUrls当中去进行合并过来)
    private val urls = LinkedHashSet<URL>()

    // 每次重启时, 需要额外去进行加载的类
    private var classLoaderFiles: ClassLoaderFiles = ClassLoaderFiles()

    // 使用BlockingQueue去维护不会产生泄露的线程列表
    private var leakSafeThreads = LinkedBlockingDeque<LeakSafeThread>()

    // 用于去进行关闭(stop)的锁, 避免出现并发stop的情况
    private val stopLock = ReentrantLock()

    init {
        // 将SilentExitExceptionHandler绑定给当前线程
        SilentExitExceptionHandler.setup(thread)

        // 修改(重设)为正确的SilentExitExceptionHandler
        this.exceptionHandler = thread.uncaughtExceptionHandler

        // 初始化时添加一个LeakSafe线程, 方便后续操作
        this.leakSafeThreads.add(LeakSafeThread())
    }

    /**
     * 获取/添加一个属性到Restarter当中
     *
     * @param name name
     * @param objectFactory ObjectFactory
     * @return 如果之前已经存在有给定的name的属性值的话, 那么return之前的值, 并且不会去进行覆盖,
     * 但是如果之前没有存在过, 那么这里将会回调"ObjectFactory.getObject"将值放入到属性值当中
     */
    open fun getOrAddAttribute(name: String, objectFactory: ObjectFactory<Any>): Any {
        synchronized(this.attributes) {
            if (!this.attributes.containsKey(name)) {
                this.attributes[name] = objectFactory.getObject()
            }
            return this.attributes[name]!!
        }
    }

    /**
     * 根据name, 从Restarter当中去移除Restarter当中的一个属性
     *
     * @param name name
     * @return 如果之前存在有属性值, return 旧的属性值; 如果不存在return null
     */
    open fun removeAttribute(name: String): Any? {
        synchronized(this.attributes) {
            return this.attributes.remove(name)
        }
    }

    /**
     * 添加在Application发生重启时, 需要额外添加的去进行加载的类
     *
     * @param classLoaderFiles 你想要添加的ClassLoaderFile列表
     */
    open fun addClassLoaderFiles(classLoaderFiles: ClassLoaderFiles) {
        this.classLoaderFiles.addAll(classLoaderFiles)
    }

    /**
     * 重启整个SpringApplication
     */
    open fun restart() {
        restart(FailureHandler.NONE)
    }

    /**
     * 重启整个SpringApplication
     *
     * @param failureHandler 处理失败的Handler
     */
    open fun restart(failureHandler: FailureHandler) {
        getLeakSafeThread().call {
            this@Restarter.stop()
            this@Restarter.start(failureHandler)
            null
        }
    }

    /**
     * 完成Restarter的初始化工作, 如果必要的话, 现在就立马去完成重启
     *
     * @param restartOnInitialize 在初始化时是否就应该去重启整个SpringApplication? 
     * 如果为true, 那么立刻完成初始化工作; 如果为false, 那么需要在文件发生改变时才去进行重启
     */
    open fun initialize(restartOnInitialize: Boolean) {
        // 将initialUrls, apply到urls列表当中
        this.initialUrls?.forEach(this.urls::add)

        // 如果需要立刻去restart, 那么需要杀掉当前线程, 并使用"restart"线程去进行重启
        if (restartOnInitialize) {
            this.immediateRestart()
        }
    }

    /**
     * 即刻去进行重启, 因为此时ApplicationContext都没准备好, 因此, 不必去完成stop;
     * 在这里我们需要使用别的线程去启动restart线程, 并将当前线程(第一次执行重启时, 也就是main线程)退出;
     * 如果我们不将当前线程退出的话, 原来的Application将和restartApplication将会并行地进行,
     * 从而会产生很多问题, 比如端口被重复使用, 因此在启用新的SpringApplication时, 我们必须将之前的线程去退出掉;
     */
    private fun immediateRestart() {
        // 使用别的线程去启动restart线程, 并且直接在这去等着它执行完
        getLeakSafeThread().callAndWait {
            this.start(FailureHandler.NONE)
            clearupCaches()
            null
        }

        // 抛出异常去退出当前线程(默默退出, 直接替换线程的ExceptionHandler去忽略掉异常)
        SilentExitExceptionHandler.exitCurrentThread()
    }

    /**
     * 关闭Restarter当中已经去进行维护的所有Root ApplicationContext
     */
    protected open fun stop() {
        stopLock.lock()  // lock
        try {
            rootContexts.forEach {
                it.close()
                // Note: 因为这里是使用COW的集合, 因此我们直接可以去remove RootContext
                // 如果不是COW的集合, 我们这里是不能去进行remove的, 会触发并发修改异常的情况
                rootContexts.remove(it)
            }
            clearupCaches()  // clear cache
        } finally {
            stopLock.unlock()  // unlock
        }
        System.gc()
        System.runFinalization()
    }

    private fun clearupCaches() {
        Introspector.flushCaches()
    }

    /**
     * 在SpringApplication开始准备(发布ApplicationPreparedEvent)时,
     * 需要将ApplicationContext去添加到Restarter当中
     *
     * @param context 需要去进行添加到Restarter当中的ApplicationContext
     */
    open fun prepare(context: ApplicationContext) {
        if (context is ConfigurableApplicationContext) {
            this.rootContexts.add(context)
        }
    }

    /**
     * 从当前的Restarter当中去移除指定的ApplicationContext
     *
     * @param context 需要去进行移除的ApplicationContext
     */
    open fun remove(context: ConfigurableApplicationContext) {
        this.rootContexts.remove(context)
    }

    /**
     * 启动Application, 使用RestartClassLoader去加载类, 并使用
     * RestartLauncher去反射执行目标main方法
     *
     * @param failureHandler 启动失败的异常处理器, 可以决策启动失败应该怎么办? 应该放弃, 还是应该重试? 
     */
    protected open fun start(failureHandler: FailureHandler) {
        while (true) {

            // doStart, 如果start成功, 那么直接return
            val error = doStart() ?: return

            // 如果Failure的决策结果是ABORT, 那么return
            if (failureHandler.handle(error) == FailureHandler.Outcome.ABORT) {
                return
            }
        }
    }

    /**
     * 获取DevTools的InitialUrls
     *
     * @return initialUrls
     * @see DefaultRestartInitializer
     * @see ChangeableUrls
     * @see DevToolsSettings
     */
    @Nullable
    open fun getInitialUrls(): Array<URL>? = this.initialUrls

    /**
     * 构建一个RestartClassLoader, 进行真正的重启
     *
     * @return 执行restart过程中的异常信息(有可能为null)
     */
    @Nullable
    protected open fun doStart(): Throwable? {
        val updatedFiles = ClassLoaderFiles(this.classLoaderFiles)
        val classLoader =
            RestartClassLoader(urls.toTypedArray(), this.applicationClassLoader, updatedFiles, this.logger)
        if (logger.isDebugEnabled) {
            logger.debug("正在使用主类[$mainClassName], URLs[$urls]去启动Application")
        }
        return relaunch(classLoader)
    }

    /**
     * 使用合适的ClassLoader(例如RestartClassLoader), 去完成真正的重启工作
     *
     * @param classLoader 要用来加载"main"类的ClassLoader
     * @return 启动过程当中出现的异常信息(有可能为null)
     */
    @Nullable
    protected open fun relaunch(classLoader: ClassLoader): Throwable? {
        val restartLauncher = RestartLauncher(this.mainClassName, this.args, classLoader, this.exceptionHandler)
        restartLauncher.start()
        restartLauncher.join()  // join for restart
        return restartLauncher.error
    }

    /**
     * 获取ThreadFactory
     *
     * @return ThreadFactory
     */
    open fun getThreadFactory(): ThreadFactory = LeakSafeThreadFactory()

    private fun getLeakSafeThread(): LeakSafeThread {
        try {
            return this.leakSafeThreads.takeFirst()
        } catch (ex: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IllegalStateException(ex)
        }
    }

    /**
     * 不被RestartClassLoader所持有的线程, 它的栈轨迹当中不含RestartClassLoader
     */
    private inner class LeakSafeThread : Thread() {

        private var callable: Callable<*>? = null

        private var result: Any? = null

        init {
            this.isDaemon = false
        }

        fun call(callable: Callable<*>) {
            this.callable = callable
            this.start()
        }

        @Suppress("UNCHECKED_CAST")
        fun <V> callAndWait(callable: Callable<V>): V? {
            this.callable = callable
            start()
            try {
                join()  // join for result
                return this.result as V?
            } catch (ex: InterruptedException) {
                currentThread().interrupt()
                throw IllegalStateException(ex)
            }
        }

        override fun run() {
            this@Restarter.leakSafeThreads.put(LeakSafeThread())
            try {
                this.result = this.callable?.call()
            } catch (ex: InterruptedException) {
                ex.printStackTrace()
                exitProcess(1)
            }
        }
    }

    private inner class LeakSafeThreadFactory : ThreadFactory {
        override fun newThread(r: Runnable): Thread {
            return getLeakSafeThread().callAndWait {
                val leakSafeThread = Thread(r)
                leakSafeThread.contextClassLoader = this@Restarter.applicationClassLoader
                leakSafeThread
            }!!
        }
    }

    companion object {
        // 对外提供Restarter的单例对象
        private var instance: Restarter? = null

        // 实例化单例对象的锁
        private val INSTANCE_MONITOR = Any()

        /**
         * 获取Restarter单例对象
         *
         * @return 单例的Restarter对象(如果还没完成初始化, return null)
         */
        @JvmStatic
        fun getInstance(): Restarter? {
            synchronized(this.INSTANCE_MONITOR) {
                return this.instance
            }
        }

        /**
         * clear掉已经初始化的Restarter
         */
        @JvmStatic
        fun clearInstance() {
            synchronized(this.INSTANCE_MONITOR) {
                this.instance = null
            }
        }

        /**
         * 设置Restarter, 去替换掉之前的Restarter
         *
         * @param instance 你想要使用的Restarter
         */
        fun setInstance(instance: Restarter) {
            synchronized(this.INSTANCE_MONITOR) {
                this.instance = instance
            }
        }

        /**
         * 初始化单例的Restarter对象(第一次完成初始化的一方还需要负责需要去创建Restarter对象)
         *
         * @param args 命令行参数
         * @param initializer Restart的初始化器
         * @param restartOnInitialize 在初始化时是否就去完成重启SpringApplication? 默认为true
         */
        @JvmStatic
        fun initialize(args: Array<String>, initializer: RestartInitializer, restartOnInitialize: Boolean = true) {
            // 因此只有第一次SpringApplication启动时, 才会去实例化并初始化Restarter
            // 因此保证了Restarter维护的一直是main线程, args一直记录的是最开始的参数
            var localInstance: Restarter? = null
            if (this.instance == null) {
                synchronized(this.INSTANCE_MONITOR) {
                    if (this.instance == null) {
                        localInstance = Restarter(Thread.currentThread(), args, initializer)
                        this.instance = localInstance
                    }
                }
            }
            localInstance?.initialize(restartOnInitialize)
        }

        /**
         * 从给定的线程的线程栈的轨迹当中, 去推断出来合适的MainClassName
         *
         * @param thread thread
         * @return mainClassName
         * @throws IllegalStateException 如果没有找到main方法的话
         */
        @JvmStatic
        @Throws(IllegalStateException::class)
        private fun getMainClassName(thread: Thread): String {
            thread.stackTrace.forEach {
                if (it.methodName == "main") {
                    val clazz = ClassUtils.forName<Any>(it.className, Restarter::class.java.classLoader)
                    val mainMethod = ReflectionUtils.findMethod(clazz, "main", Array<String>::class.java)!!
                    return mainMethod.declaringClass.name
                }
            }
            throw IllegalStateException("无法从目标线程[$thread]的栈轨迹当中去去获取到main方法")
        }
    }
}