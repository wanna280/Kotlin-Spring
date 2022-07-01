package com.wanna.framework.aop.interceptor

import org.slf4j.LoggerFactory
import java.lang.reflect.Method

/**
 * 简单处理异步方法异常的AsyncExceptionHandler，只是简单打印一下日志，别的啥也不做
 *
 * @see AsyncUncaughtExceptionHandler
 */
class SimpleAsyncUncaughtExceptionHandler : AsyncUncaughtExceptionHandler {
    companion object {
        private val logger = LoggerFactory.getLogger(SimpleAsyncUncaughtExceptionHandler::class.java)
    }

    override fun handleException(ex: Throwable, method: Method, vararg args: Any?) {
        if (logger.isErrorEnabled) {
            logger.error("执行异步方法[${method.toGenericString()}]过程中，出现异常，原因是[$ex]")
        }
    }
}