package com.wanna.framework.web.http.converter

import com.wanna.framework.web.http.HttpInputMessage
import com.wanna.framework.web.http.HttpOutputMessage
import com.wanna.framework.web.http.MediaType

/**
 * 对ByteArray("byte[]")提供支持的MessageConverter
 *
 * @see AbstractHttpMessageConverter
 * @see HttpMessageConverter
 */
open class ByteArrayHttpMessageConverter : AbstractHttpMessageConverter<ByteArray>() {
    init {
        setSupportedMediaTypes(MediaType.APPLICATION_OCTET_STREAM, MediaType.ALL)
    }

    override fun supports(clazz: Class<*>) = clazz == ByteArray::class.java

    override fun readInternal(clazz: Class<*>, inputMessage: HttpInputMessage): ByteArray {
        return inputMessage.getBody().readAllBytes()
    }

    override fun writeInternal(t: ByteArray, mediaType: MediaType?, outputMessage: HttpOutputMessage) {
        outputMessage.getBody().write(t)
        outputMessage.getBody().flush()  // flush
    }
}