package com.wanna.boot

import com.wanna.boot.BootstrapRegistry.InstanceSupplier
import com.wanna.boot.BootstrapRegistry.Scope
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.context.event.ApplicationListener
import com.wanna.framework.context.event.SimpleApplicationEventMulticaster
import java.util.function.Supplier

/**
 * 默认的BootstrapContext的实现
 *
 * @see ConfigurableBootstrapContext
 * @see BootstrapContext
 * @see BootstrapRegistry
 */
open class DefaultBootstrapContext : ConfigurableBootstrapContext {

    /**
     * InstanceSupplier的列表, Key是类型, Value是该类型对应的InstanceSupplier
     */
    private val instanceSuppliers = HashMap<Class<*>, InstanceSupplier<*>>()

    /**
     * 单例的Instance列表, 如果InstanceSupplier是单例的话, 那么创建之后就会被缓存到这个Map当中
     */
    private val instances = HashMap<Class<*>, Any>()

    /**
     * ApplicationEvent的派发器
     */
    private val events = SimpleApplicationEventMulticaster()

    /**
     * 根据type从BootstrapContext当中去获取到对应的实例
     *
     * @param type type
     * @param T type
     * @return 根据type类型获取到的实例对象
     * @throws IllegalStateException 如果之前没有注册过该类型的实例
     */
    override fun <T> get(type: Class<T>): T =
        getOrElseThrow(type, IllegalStateException("给定的type=[${type.name}]没有被注册过"))

    /**
     * 根据type从BootstrapContext当中去获取到对应的实例, 如果不存在的话, 那么返回给定的默认值
     *
     * @param type type
     * @param T type
     * @param other 默认的对象
     * @return 根据type类型获取到的实例对象
     */
    override fun <T> getOrElse(type: Class<T>, other: T): T = getOrElseSupply(type) {
        other
    }

    /**
     * 根据type从BootstrapContext当中去获取到对应的实例, 如果不存在的话, 那么返回给定的默认值
     *
     * @param type type
     * @param T type
     * @param other 创建默认对象的Supplier
     * @return 根据type类型获取到的实例对象
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T> getOrElseSupply(type: Class<T>, other: Supplier<T>): T {
        synchronized(this.instanceSuppliers) {
            val instanceSupplier = this.instanceSuppliers[type]
            return if (instanceSupplier == null) other.get()
            else getInstance(type, instanceSupplier as InstanceSupplier<T>)
        }
    }

    /**
     * 根据type从BootstrapContext当中去获取到对应的实例, 如果不存在的话, 那么返回给定的默认值
     *
     * @param type type
     * @param T type
     * @param exceptionSupplier 丢出异常的Supplier
     * @return 根据type类型获取到的实例对象
     * @throws IllegalStateException 如果之前没有注册过该类型的实例
     */
    override fun <T, X : Throwable> getOrElseThrow(type: Class<T>, exceptionSupplier: Supplier<out X>) =
        getOrElseThrow(type, exceptionSupplier.get())

    /**
     * 判断当前的BootstrapContext当中是否已经注册了给定类型的实例? 
     *
     * @param type type
     * @param T Type
     * @return 如果已经注册过, return true; 否则return false
     */
    override fun <T> isRegistered(type: Class<T>): Boolean {
        synchronized(this.instanceSuppliers) {
            return this.instanceSuppliers.containsKey(type)
        }
    }

    /**
     * 添加一个处理BootstrapContext关闭的事件的监听器
     *
     * @param listener listener
     */
    override fun addCloseListener(listener: ApplicationListener<BootstrapContextClosedEvent>) =
        events.addApplicationListener(listener)

    /**
     * 注册一个具体类型的实例对象到BootstrapRegistry当中
     *
     * @param type type
     * @param instanceSupplier 创建对象的InstanceSupplier
     */
    override fun <T> register(type: Class<T>, instanceSupplier: InstanceSupplier<T>) =
        register(type, instanceSupplier, true)

    /**
     * 如果之前没有该类型的对象的话, 那么去注册一个到BootstrapRegistry当中; 如果之前已经有的话, 那么就不必注册
     *
     * @param type type
     * @param instanceSupplier 创建对象的InstanceSupplier
     */
    override fun <T> registerIfAbsent(type: Class<T>, instanceSupplier: InstanceSupplier<T>) =
        register(type, instanceSupplier, false)

    /**
     * 根据类型去获取它所注册的InstanceSupplier
     *
     * @param type type
     * @return 该类型对于的InstanceSupplier(如果不存在该类型对应的实例, 那么return null)
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T> getRegisteredInstanceSupplier(type: Class<T>): InstanceSupplier<T>? {
        synchronized(this.instanceSuppliers) {
            return instanceSuppliers[type] as InstanceSupplier<T>?
        }
    }


    /**
     * 根据type从BootstrapContext当中去获取到对应的实例, 如果不存在的话, 那么抛出异常
     *
     * @param type type
     * @param T type
     * @param exception 获取不到时需要抛出的异常
     * @return 根据type类型获取到的实例对象
     * @throws Throwable 如果获取不到该类型的对象的话
     */
    @Throws(Throwable::class)
    @Suppress("UNCHECKED_CAST")
    private fun <T> getOrElseThrow(type: Class<T>, exception: Throwable): T {
        synchronized(instanceSuppliers) {
            val instanceSupplier = instanceSuppliers[type] ?: throw exception
            return getInstance(type, instanceSupplier as InstanceSupplier<T>)
        }
    }

    /**
     * 注册一个InstanceSupplier到当前的BootstrapContext当中
     *
     * @param type type
     * @param instanceSupplier InstanceSupplier
     * @param replaceExisting 是否需要替换掉已经存在的? 
     * @throws IllegalStateException 如果之前已经存在过实例, 还去进行初始
     */
    private fun <T> register(type: Class<T>, instanceSupplier: InstanceSupplier<T>, replaceExisting: Boolean) {
        synchronized(this.instanceSuppliers) {
            if (replaceExisting || !instanceSuppliers.containsKey(type)) {
                if (instances.containsKey(type)) {
                    throw IllegalStateException("${type.name}已经被注册过了")
                }
                instanceSuppliers[type] = instanceSupplier
            }
        }
    }

    /**
     * 根据type和InstanceSupplier去获取到实例对象;
     * 如果instances缓存当中已经有了该类型对应的对象的话, 那么直接从缓存获取即可;
     * 如果Instances缓存当中没有的话, 那么使用给定的InstanceSupplier去进行创建
     *
     * @param type type
     * @param instanceSupplier InstanceSupplier
     * @return 获取到的实例对象
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T> getInstance(type: Class<T>, instanceSupplier: InstanceSupplier<T>): T {
        var instance = instances[type]
        if (instance == null) {
            instance = instanceSupplier.get(this)
            if (instanceSupplier.getScope() == Scope.SINGLETON) {
                instances[type] = instance!!
            }
        }
        return instance as T
    }

    /**
     * 关闭BootstrapContext, 触发BootstrapContextClosedEvent
     *
     * @param context ApplicationContext
     */
    open fun close(context: ConfigurableApplicationContext) {
        events.multicastEvent(BootstrapContextClosedEvent(this, context))
    }
}