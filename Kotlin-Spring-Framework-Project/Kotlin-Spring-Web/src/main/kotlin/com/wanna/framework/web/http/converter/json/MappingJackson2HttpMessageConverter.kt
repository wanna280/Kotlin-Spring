package com.wanna.framework.web.http.converter.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.wanna.framework.web.http.HttpInputMessage
import com.wanna.framework.web.http.HttpOutputMessage
import com.wanna.framework.web.http.MediaType
import com.wanna.framework.web.http.converter.HttpMessageConverter

/**
 * 这是一个利用Jackson2去进行HTTP消息的转换的MessageConverter
 */
open class MappingJackson2HttpMessageConverter : HttpMessageConverter<Any> {

    private val objectMapper: ObjectMapper = ObjectMapper()

    override fun getSupportedMediaTypes(): List<MediaType> {
        return arrayListOf(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
    }

    override fun canRead(clazz: Class<*>, mediaType: MediaType?): Boolean {
        return true
    }

    override fun canWrite(clazz: Class<*>, mediaType: MediaType?): Boolean {
        return true
    }

    override fun read(clazz: Class<Any>, inputMessage: HttpInputMessage): Any {
        return objectMapper.readValue(inputMessage.getBody(), clazz)
    }

    override fun write(t: Any, mediaType: MediaType?, outputMessage: HttpOutputMessage) {
        objectMapper.writeValue(outputMessage.getBody(), t)
        outputMessage.getBody().flush()  // flush
    }
}