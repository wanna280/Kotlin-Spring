package com.wanna.framework.web.http.client

import com.wanna.framework.web.bind.annotation.RequestMethod
import java.net.URI

/**
 * 这是一个ClientHttpRequest的Factory, 负责完成ClientHttpRequest的创建工作
 *
 * @see ClientHttpRequest
 */
interface ClientHttpRequestFactory {
    fun createRequest(url: URI, method: RequestMethod): ClientHttpRequest
}