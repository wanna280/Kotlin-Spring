package com.wanna.framework.web.http

import com.wanna.framework.web.bind.annotation.RequestMethod
import java.net.URI

/**
 * Http请求的封装
 */
interface HttpRequest : HttpMessage {
    fun getMethod(): RequestMethod
    fun getUri(): URI
}