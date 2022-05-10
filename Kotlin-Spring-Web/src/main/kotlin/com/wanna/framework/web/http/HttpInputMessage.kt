package com.wanna.framework.web.http

import java.io.InputStream

/**
 * HttpRequest的输入流，支持从InputStream当中去获取RequestBody当中的数据
 */
interface HttpInputMessage : HttpMessage {
    fun getBody(): InputStream
}