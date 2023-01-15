package com.wanna.framework.web.http

import java.io.OutputStream

/**
 * 这是Http的输出的消息
 *
 * * 1.对于Client来说, 可以从OutputStream当中获取到Client要发送给Server的RequestBody
 * * 2.对于Server来说, 可以获取将ResponseBody写入到OutputStream当中
 */
interface HttpOutputMessage : HttpMessage {
    fun getBody(): OutputStream
}