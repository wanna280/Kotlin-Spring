package com.wanna.framework.web.http.converter

import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.http.HttpInputMessage
import com.wanna.framework.web.http.HttpOutputMessage
import com.wanna.framework.web.http.MediaType
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * 处理String的MessageConverter
 *
 * @see HttpMessageConverter
 * @see AbstractHttpMessageConverter
 *
 * @param defaultCharset 默认字符集
 */
open class StringHttpMessageConverter(defaultCharset: Charset = DEFAULT_CHARSET) :
    AbstractHttpMessageConverter<String>() {
    companion object {
        // 默认的字符集
        @JvmStatic
        val DEFAULT_CHARSET: Charset = StandardCharsets.ISO_8859_1
    }

    init {
        // init SupportedMediaTypes
        setSupportedMediaTypes(MediaType.TEXT_PLAIN, MediaType.ALL)
        setDefaultCharset(defaultCharset)
    }

    override fun supports(clazz: Class<*>) = clazz == String::class.java

    override fun readInternal(clazz: Class<*>, inputMessage: HttpInputMessage): String {
        return String(
            inputMessage.getBody().readAllBytes(),
            getContentTypeCharset(inputMessage.getHeaders().getContentType())
        )
    }

    override fun writeInternal(t: String, @Nullable mediaType: MediaType?, outputMessage: HttpOutputMessage) {
        outputMessage.getBody().write(t.toByteArray(getContentTypeCharset(mediaType)))
        outputMessage.getBody().flush() // flush
    }

    private fun getContentTypeCharset(@Nullable mediaType: MediaType?): Charset {
        if (mediaType != null) {
            val charset = mediaType.charset
            if (charset != null) {
                return charset
            }
            if (mediaType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
                return StandardCharsets.UTF_8
            }
        }
        return getDefaultCharset() ?: throw IllegalStateException("没有默认的Charset")
    }
}