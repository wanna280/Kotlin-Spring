package com.wanna.framework.web.client

import com.wanna.common.logging.Logger
import com.wanna.common.logging.LoggerFactory
import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.http.MediaType
import com.wanna.framework.web.http.client.ClientHttpResponse
import com.wanna.framework.web.http.converter.HttpMessageConverter

/**
 * 基于HttpMessageConverter去进行请求的提取, 将ResponseBody提取成为JavaBean
 *
 * @param messageConverters 要去进行读取数据的HttpMessageConverter列表
 * @param responseType 想要使用响应类型
 * @param logger Logger
 *
 * @param T 需要去进行提取的数据的目标类型
 */
open class HttpMessageConverterExtractor<T>(
    private val messageConverters: List<HttpMessageConverter<*>>,
    private val responseType: Class<T>,
    @Nullable logger: Logger? = null
) : ResponseExtractor<T> {

    /**
     * Logger
     */
    private val logger: Logger = logger ?: LoggerFactory.getLogger(javaClass)

    /**
     * 从[response]当中提取出来数据
     *
     * @param response response
     * @return 从response当中提取到的数据
     */
    @Nullable
    @Suppress("UNCHECKED_CAST")
    override fun extractData(response: ClientHttpResponse): T? {
        // 获取本次响应的Content-Type
        val contentType = getContentType(response)

        // 让所有MessageConverter来尝试一下...看它是否支持处理这样的响应?
        for (converter in messageConverters) {
            if (converter.canRead(this.responseType, contentType)) {
                return (converter as HttpMessageConverter<T>).read(responseType, response)
            }
        }
        return null
    }

    /**
     * 获取响应类型
     *
     * @param response HttpResponse
     */
    private fun getContentType(response: ClientHttpResponse): MediaType {
        val contentType = response.getHeaders().getContentType()

        // 如果没有Content-Type的话, 那么将会使用"application/octet-stream"
        if (contentType === null && logger.isTraceEnabled) {
            logger.trace("No content-type, using 'application/octet-stream'")
        }
        return contentType ?: MediaType.APPLICATION_OCTET_STREAM
    }
}