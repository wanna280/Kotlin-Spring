package com.wanna.cloud.openfeign.support

import com.wanna.framework.web.http.HttpHeaders
import com.wanna.framework.web.http.HttpInputMessage
import com.wanna.framework.web.http.converter.HttpMessageConverter
import feign.Response
import java.lang.reflect.Type
import feign.codec.Decoder
import java.io.InputStream

/**
 * SpringDecoder, 负责将本次请求的Response, 编码成为返回值的JavaBean去进行返回
 *
 * @see Decoder
 * @param messageConverters Web当中的MessageConverter列表
 */
@Suppress("UNCHECKED_CAST")
open class SpringDecoder(private val messageConverters: List<HttpMessageConverter<*>>) : Decoder {

    override fun decode(response: Response, type: Type): Any? {
        if (messageConverters.isEmpty()) {
            return null
        }
        val converter = messageConverters[0] as HttpMessageConverter<Any>
        return converter.read(type as Class<Any>, FeignHttpInputMessage(response))
    }

    private class FeignHttpInputMessage(private val response: Response) : HttpInputMessage {
        override fun getBody(): InputStream {
            return response.body().asInputStream()
        }

        override fun getHeaders() = HttpHeaders()
    }
}