package com.wanna.framework.web.client

import com.wanna.framework.web.http.MediaType
import com.wanna.framework.web.http.client.ClientHttpResponse
import com.wanna.framework.web.http.converter.HttpMessageConverter

/**
 * 基于HttpMessageConverter去进行请求的提取, 将ResponseBody提取成为JavaBean
 *
 * @param messageConverters 要去进行读取数据的HttpMessageConverter列表
 * @param responseType 想要使用响应类型
 */
open class HttpMessageConverterExtractor<T>(
    private val messageConverters: List<HttpMessageConverter<*>>,
    private val responseType: Class<T>
) : ResponseExtractor<T> {

    @Suppress("UNCHECKED_CAST")
    override fun extractData(response: ClientHttpResponse): T? {
        messageConverters.forEach {
            if (it.canRead(responseType, MediaType.APPLICATION_JSON)) {
                return (it as HttpMessageConverter<T>).read(responseType, response)
            }
        }
        return null
    }
}