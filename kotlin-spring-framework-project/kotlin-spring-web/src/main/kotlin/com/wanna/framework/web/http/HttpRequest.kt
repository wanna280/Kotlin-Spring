package com.wanna.framework.web.http

import com.wanna.framework.web.bind.annotation.RequestMethod
import java.net.URI

/**
 * Http请求的封装
 */
interface HttpRequest : HttpMessage {

    /**
     * 获取HTTP的请求方式
     *
     * @return HTTP请求方式
     */
    fun getMethod(): RequestMethod

    /**
     * 获取HTTP请求的URI
     *
     * @return HTTP请求URI
     */
    fun getURI(): URI
}