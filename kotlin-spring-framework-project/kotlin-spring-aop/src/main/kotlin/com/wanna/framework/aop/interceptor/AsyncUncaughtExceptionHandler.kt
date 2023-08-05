package com.wanna.framework.aop.interceptor

import java.lang.reflect.Method

/**
 * 异步任务当中没有去进行捕获的异常的处理器
 */
@FunctionalInterface
fun interface AsyncUncaughtExceptionHandler {

    /**
     * 处理异步方法的异常
     *
     * @param ex 要处理的异常
     * @param method 发生异常的目标方法
     * @param args 该方法的相关参数
     */
    fun handleException(ex: Throwable, method: Method, vararg args: Any?)
}