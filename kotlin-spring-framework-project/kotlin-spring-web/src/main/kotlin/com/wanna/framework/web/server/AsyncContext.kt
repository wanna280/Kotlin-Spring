package com.wanna.framework.web.server

/**
 * 为异步任务提供支持的Context上下文信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/13
 */
interface AsyncContext {

    /**
     * 获取request
     *
     * @return request
     */
    fun getRequest(): HttpServerRequest?

    /**
     * 获取response
     *
     * @return response
     */
    fun getResponse(): HttpServerResponse?

    /**
     * 派发异步任务的执行结果
     */
    fun dispatch()

    /**
     * 派发异步任务的执行结果
     */
    fun dispatch(path: String)

    /**
     * 完成异步任务
     */
    fun complete()

    /**
     * 设置超时时间
     *
     * @param timeout timeout
     */
    fun setTimeout(timeout: Long)

    /**
     * 获取超时时间
     *
     * @return 超时时间
     */
    fun getTimeout(): Long
}