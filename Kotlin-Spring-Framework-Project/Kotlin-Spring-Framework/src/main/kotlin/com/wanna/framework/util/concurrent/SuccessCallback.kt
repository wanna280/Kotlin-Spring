package com.wanna.framework.util.concurrent

/**
 * 处理成功的回调
 */
@FunctionalInterface
interface SuccessCallback<T> {
    fun onSuccess(result: T?)
}