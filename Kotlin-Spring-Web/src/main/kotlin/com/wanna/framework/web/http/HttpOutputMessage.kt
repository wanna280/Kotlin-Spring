package com.wanna.framework.web.http

import java.io.OutputStream

/**
 * 这是HttpResponse的输出流，可以将要发送给客户端的消息，写入到输出流当中
 */
interface HttpOutputMessage : HttpMessage {
    fun getBody(): OutputStream
}