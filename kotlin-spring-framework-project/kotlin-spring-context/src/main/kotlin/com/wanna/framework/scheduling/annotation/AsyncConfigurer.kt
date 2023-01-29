package com.wanna.framework.scheduling.annotation

import com.wanna.framework.aop.interceptor.AsyncUncaughtExceptionHandler
import java.util.concurrent.Executor

/**
 * 异步任务的配置器
 */
interface AsyncConfigurer {
    fun getAsyncExecutor(): Executor? = null
    fun getAsyncUncaughtExceptionHandler(): AsyncUncaughtExceptionHandler? = null
}