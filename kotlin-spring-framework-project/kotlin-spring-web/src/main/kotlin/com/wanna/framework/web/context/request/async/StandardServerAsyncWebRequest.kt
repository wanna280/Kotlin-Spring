package com.wanna.framework.web.context.request.async

import com.wanna.framework.web.context.request.ServerWebRequest
import com.wanna.framework.web.server.ActionCode
import com.wanna.framework.web.server.AsyncContext
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

/**
 * 对于AsyncWebRequest的标准实现
 *
 * @param request request
 * @param response response
 */
open class StandardServerAsyncWebRequest(request: HttpServerRequest, response: HttpServerResponse) :
    ServerWebRequest(request, response), AsyncWebRequest {

    /**
     * AsyncContext
     */
    private var asyncContext: AsyncContext? = null

    override fun isAsyncStarted(): Boolean {
        return asyncContext != null
    }

    override fun startAsync() {
        if (!this.isAsyncStarted()) {
            val request = getNativeRequest(HttpServerRequest::class.java)
            val response = getNativeResponse(HttpServerResponse::class.java)!!
            this.asyncContext = request.startAsync(request, response)
        }
    }

    override fun dispatch() {
        asyncContext?.dispatch()
    }
}