package com.wanna.framework.web.context.request.async

import com.wanna.framework.web.context.request.NativeWebRequest

/**
 * 异步的Web请求
 */
interface AsyncWebRequest : NativeWebRequest {

    /**
     * 该异步任务是否已经开始？
     *
     * @return 如果已经开始return true；否则return false
     */
    fun isAsyncStarted(): Boolean

    /**
     * 派发异步任务
     */
    fun dispatch()
}