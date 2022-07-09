package com.wanna.boot.devtools.restart

import com.wanna.framework.core.util.ClassUtils
import com.wanna.framework.core.util.ReflectionUtils

/**
 * 一个用于SpringBoot-Devtools去进行重启的线程
 *
 * @param mainClassName mainClassName
 * @param args 启动参数
 * @param contextClassLoader Thread的ContextClassLoader
 * @param uncaughtExceptionHandler ExceptionHandler
 */
class RestartLauncher(
    private val mainClassName: String,
    private val args: Array<String>,
    contextClassLoader: ClassLoader,
    uncaughtExceptionHandler: UncaughtExceptionHandler
) : Thread() {

    // 记录重启SpringApplication的过程当中出现的Error
    var error: Throwable? = null

    init {
        setContextClassLoader(contextClassLoader)
        setUncaughtExceptionHandler(uncaughtExceptionHandler)
        this.name = "restartedMain"  // restart Thread Name
        this.isDaemon = false
    }

    /**
     * 重启的方式，使用具体的ClassLoader，去获取到mainClass的"main"方法，并反射调用，
     * 去执行SpringBoot Application的重启工作
     */
    override fun run() {
        try {
            val mainClass = ClassUtils.forName<Any>(mainClassName, contextClassLoader)
            val mainMethod = ReflectionUtils.findMethod(mainClass, "main", Array<String>::class.java)!!
            ReflectionUtils.invokeMethod(mainMethod, mainClass, args)
        } catch (ex: Throwable) {
            this.error = ex  // record error
            uncaughtExceptionHandler.uncaughtException(this, ex)
        }
    }
}