package com.wanna.cloud.openfeign.support

import com.wanna.framework.web.http.HttpHeaders
import com.wanna.framework.web.http.HttpOutputMessage
import com.wanna.framework.web.http.MediaType
import com.wanna.framework.web.http.converter.HttpMessageConverter
import feign.RequestTemplate
import feign.codec.Encoder
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.lang.reflect.Type

/**
 * Encoder, 将给定的Java对象, 直接转换为数据, 写入到请求体当中
 *
 * @see Encoder
 * @param messageConverters Web当中的MessageConverter列表
 */
@Suppress("UNCHECKED_CAST")
open class SpringEncoder(private val messageConverters: List<HttpMessageConverter<*>>) : Encoder {
    override fun encode(`object`: Any?, bodyType: Type?, template: RequestTemplate?) {
        if (`object` != null && bodyType != null && template != null && messageConverters.isNotEmpty()) {
            val converter = messageConverters[0] as HttpMessageConverter<Any>
            converter.write(bodyType as Class<Any>, MediaType.APPLICATION_JSON, FeignHttpOutputMessage(template))
        }
    }

    private class FeignHttpOutputMessage(private val template: RequestTemplate) : HttpOutputMessage {
        override fun getHeaders() = HttpHeaders()
        override fun getBody(): OutputStream {
            val baos = ByteArrayOutputStream(template.body().size)
            baos.write(template.body())
            return baos
        }
    }
}