package com.wanna.framework.web.http

import java.io.InputStream

/**
 * 这是对Http的输入消息的描述
 *
 * * 1.对于Client而言, 可以获取Server发送过来的ResponseBody
 * * 2.对于Server而言, 可以从InputStream当中解析到Client发送的RequestBody
 */
interface HttpInputMessage : HttpMessage {

    /**
     * 获取到InputMessage, 也就是HTTP请求的输入流
     *
     * @return InputStream
     */
    fun getBody(): InputStream
}