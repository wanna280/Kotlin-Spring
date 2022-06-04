package com.wanna.framework.web.http.client

import com.wanna.framework.web.http.HttpInputMessage

/**
 * 客户端的Http响应
 */
interface ClientHttpResponse : HttpInputMessage {
    fun getStatusCode() : Int
}