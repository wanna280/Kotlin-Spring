package com.wanna.framework.util.concurrent

import java.util.concurrent.Future

/**
 * 可以支持去监听任务的处理结果的Future
 *
 * @see Future
 * @see ListenableFutureCallback
 */
interface ListenableFuture<T> : Future<T> {
    fun addCallback(successCallback: SuccessCallback<T>, failureCallback: FailureCallback)
    fun addCallback(callback: ListenableFutureCallback<T>)
}