package com.wanna.boot.devtools.restart

import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.ReflectionUtils

/**
 * 一个用于"SpringBoot-Devtools"去进行重启的线程
 *
 * @param mainClassName mainClassName
 * @param args 启动参数
 * @param contextClassLoader Thread的ContextClassLoader
 * @param uncaughtExceptionHandler ExceptionHandler
 */
open class RestartLauncher(
    private val mainClassName: String,
    private val args: Array<String>,
    contextClassLoader: ClassLoader,
    uncaughtExceptionHandler: UncaughtExceptionHandler
) : Thread() {

    // 记录重启SpringApplication的过程当中出现的Error
    var error: Throwable? = null

    init {
        this.contextClassLoader = contextClassLoader
        this.uncaughtExceptionHandler = uncaughtExceptionHandler
        this.name = "restartedMain"  // restart Thread Name
        this.isDaemon = false
    }

    /**
     * 重启的方式, 使用具体的ClassLoader(例如RestartClassLoader), 去获取到mainClass的"main"方法, 并反射调用,
     * 去执行SpringBoot Application的重启工作
     *
     * @see com.wanna.boot.devtools.restart.classloader.RestartClassLoader
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