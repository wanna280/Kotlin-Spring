package com.wanna.framework.util.concurrent

import java.util.*
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask

/**
 * 基于FutureTask去实现的ListenableFuture
 *
 * @param callable callback
 */
open class ListenableFutureTask<T>(callable: Callable<T>) : FutureTask<T>(callable), ListenableFuture<T> {
    private val callbacks = ListenableFutureCallbackRegistry<T>()

    override fun addCallback(successCallback: SuccessCallback<T>, failureCallback: FailureCallback) {
        this.callbacks.addSuccessCallback(successCallback)
        this.callbacks.addFailureCallback(failureCallback)
    }

    override fun addCallback(callback: ListenableFutureCallback<T>) {
        this.callbacks.addSuccessCallback(callback)
        this.callbacks.addFailureCallback(callback)
    }

    /**
     * 在任务执行完成之后，需要回调所有的ListenableCallback，结果已经产生了
     */
    override fun done() {
        var cause: Throwable? = null
        try {
            val result = get()
            this.callbacks.success(result)
        } catch (ex: ExecutionException) {
            cause = ex.cause
        } catch (ex: Throwable) {
            cause = ex
        }
        Optional.ofNullable(cause).ifPresent(callbacks::failure)
    }
}