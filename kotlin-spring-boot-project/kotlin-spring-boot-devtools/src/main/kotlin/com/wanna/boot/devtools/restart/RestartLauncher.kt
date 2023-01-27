package com.wanna.boot.devtools.restart

import com.wanna.boot.devtools.restart.classloader.RestartClassLoader
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.ReflectionUtils

/**
 * 用于"SpringBoot-Devtools"去进行重启的线程, 用于使用给定的[ClassLoader]去加载到mainClass,
 * 并使用反射去找到该类当中的main方法, 执行SpringApplication的启动
 *
 * @param mainClassName mainClassName, 住启动类
 * @param args main方法的启动参数
 * @param contextClassLoader Thread的ContextClassLoader
 * @param uncaughtExceptionHandler 处理未被catch住的情况下, 线程应该怎么处理抛出来的异常的ExceptionHandler
 *
 * @see RestartClassLoader
 */
open class RestartLauncher(
    private val mainClassName: String,
    private val args: Array<String>,
    contextClassLoader: ClassLoader,
    uncaughtExceptionHandler: UncaughtExceptionHandler
) : Thread() {

    /**
     * 记录重启SpringApplication的过程当中出现异常情况
     */
    @Nullable
    var error: Throwable? = null

    init {
        this.contextClassLoader = contextClassLoader
        this.uncaughtExceptionHandler = uncaughtExceptionHandler
        this.name = "restartedMain"  // restart Thread Name
        this.isDaemon = false
    }

    /**
     * 重启的方式, 使用具体的ClassLoader(例如[RestartClassLoader]), 去获取到mainClass的"main"方法,
     * 并使用反射的方式去调用main方法, 从而去执行SpringBoot Application的重启工作
     *
     * @see RestartClassLoader
     */
    override fun run() {
        try {
            val mainClass = ClassUtils.forName<Any>(this.mainClassName, this.contextClassLoader)
            val mainMethod = ReflectionUtils.findMethod(mainClass, "main", Array<String>::class.java)!!
            ReflectionUtils.invokeMethod(mainMethod, mainClass, this.args)
        } catch (ex: Throwable) {
            this.error = ex  // record error
            uncaughtExceptionHandler.uncaughtException(this, ex)
        }
    }
}