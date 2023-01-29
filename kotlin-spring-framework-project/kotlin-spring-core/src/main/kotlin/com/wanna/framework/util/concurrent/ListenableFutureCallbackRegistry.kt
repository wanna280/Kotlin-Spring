package com.wanna.framework.util.concurrent

/**
 * ListenableFuture的Callback的注册中心
 *
 * @see SuccessCallback
 * @see FailureCallback
 */
open class ListenableFutureCallbackRegistry<T> {
    private val successCallbacks = ArrayList<SuccessCallback<T>>()
    private val failureCallbacks = ArrayList<FailureCallback>()

    open fun addSuccessCallback(successCallback: SuccessCallback<T>) {
        this.successCallbacks += successCallback
    }

    open fun addFailureCallback(failureCallback: FailureCallback) {
        this.failureCallbacks += failureCallback
    }

    open fun success(result: T?) {
        successCallbacks.forEach {
            it.onSuccess(result)
        }
    }

    open fun failure(ex: Throwable) {
        failureCallbacks.forEach {
            it.onError(ex)
        }
    }
}