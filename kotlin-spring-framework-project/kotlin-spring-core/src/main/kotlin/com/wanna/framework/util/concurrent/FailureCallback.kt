package com.wanna.framework.util.concurrent

/**
 * 处理失败的结果的Callback
 */
@FunctionalInterface
interface FailureCallback {
    fun onError(ex: Throwable)
}