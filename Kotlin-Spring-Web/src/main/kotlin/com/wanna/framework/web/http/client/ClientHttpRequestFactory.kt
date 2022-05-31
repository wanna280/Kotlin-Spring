package com.wanna.framework.web.http.client

import com.wanna.framework.web.bind.RequestMethod
import com.wanna.framework.web.http.client.ClientHttpRequest
import java.net.URI

/**
 * 这是一个ClientHttpRequest的Factory，负责完成ClientHttpRequest的创建工作
 *
 * @see ClientHttpRequest
 */
interface ClientHttpRequestFactory {
    fun create(url: URI, method: RequestMethod): ClientHttpRequest
}