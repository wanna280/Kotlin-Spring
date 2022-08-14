package com.wanna.framework.web.http.converter.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.wanna.framework.web.http.HttpInputMessage
import com.wanna.framework.web.http.HttpOutputMessage
import com.wanna.framework.web.http.MediaType
import com.wanna.framework.web.http.converter.AbstractHttpMessageConverter
import com.wanna.framework.web.http.converter.HttpMessageConverter
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * 这是一个利用Jackson2的ObjectMapper去进行HTTP消息的转换的MessageConverter
 *
 * @see ObjectMapper
 */
open class MappingJackson2HttpMessageConverter : AbstractHttpMessageConverter<Any>() {
    companion object {
        // 默认的字符集为UTF-8
        @JvmStatic
        val DEFAULT_CHARSET: Charset = StandardCharsets.UTF_8
    }

    private val objectMapper: ObjectMapper = ObjectMapper()

    init {
        setSupportedMediaTypes(MediaType.APPLICATION_JSON)
        setDefaultCharset(DEFAULT_CHARSET)
    }

    /**
     * 不管是什么类型的数据，我都支持去进行处理，因为我是Json的序列化方式
     *
     * @param clazz JavaBean类型
     */
    override fun supports(clazz: Class<*>) = true

    /**
     * readInternal，直接利用ObjectMapper去进行读取即可
     *
     * @param clazz 目标数据类型
     * @param inputMessage request Message
     */
    override fun readInternal(clazz: Class<*>, inputMessage: HttpInputMessage): Any =
        objectMapper.readValue(inputMessage.getBody(), clazz)

    /**
     * writeInternal，直接利用ObjectMapper去进行write即可
     *
     * @param t 要去进行写入的目标类型
     * @param mediaType MediaType
     * @param outputMessage response Message
     */
    override fun writeInternal(t: Any, mediaType: MediaType?, outputMessage: HttpOutputMessage) {
        objectMapper.writeValue(outputMessage.getBody(), t)
        outputMessage.getBody().flush()  // flush
    }
}