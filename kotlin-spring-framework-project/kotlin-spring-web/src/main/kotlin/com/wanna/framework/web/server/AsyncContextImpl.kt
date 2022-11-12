package com.wanna.framework.web.server

/**
 * 默认的[AsyncContext]的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/13
 */
open class AsyncContextImpl : AsyncContext {

    /**
     * request
     */
    private var request: HttpServerRequest? = null

    /**
     * response
     */
    private var response: HttpServerResponse? = null

    /**
     * timeout
     */
    private var timeout: Long = 0L

    open fun setStarted(request: HttpServerRequest?, response: HttpServerResponse?) {
        this.request = request
        this.response = response
    }

    override fun getRequest(): HttpServerRequest? = this.request

    override fun getResponse(): HttpServerResponse? = this.response

    override fun dispatch() {
        request?.action(ActionCode.ASYNC_DISPATCH, this)
    }

    override fun dispatch(path: String) {
        request?.action(ActionCode.ASYNC_DISPATCH, this)
    }

    override fun complete() {

    }

    override fun setTimeout(timeout: Long) {
        this.timeout = timeout
    }

    override fun getTimeout(): Long = this.timeout
}