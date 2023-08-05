package com.wanna.framework.web.context.request.async

import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.server.AsyncContext

/**
 * 异步的Web请求, 内部会封装[AsyncContext]去提供异步的相关支持
 *
 * @see AsyncContext
 */
interface AsyncWebRequest : NativeWebRequest {
    /**
     * 该异步任务是否已经开始?
     *
     * @return 如果已经开始return true; 否则return false
     */
    fun isAsyncStarted(): Boolean

    /**
     * 开始异步任务
     */
    fun startAsync()

    /**
     * 派发异步任务的执行结果
     */
    fun dispatch()
}