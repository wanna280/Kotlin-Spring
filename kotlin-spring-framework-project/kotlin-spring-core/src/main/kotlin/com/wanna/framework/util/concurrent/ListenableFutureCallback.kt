package com.wanna.framework.util.concurrent

/**
 * 监听任务的执行结果的Future的Callback, 同时处理成功和失败的情况
 *
 * @see FailureCallback
 * @see SuccessCallback
 */
interface ListenableFutureCallback<T> : FailureCallback, SuccessCallback<T>