package com.wanna.framework.web.client

import com.wanna.framework.web.http.client.ClientHttpResponse

/**
 * 它是一个Response的提取器(Extractor)，负责将ClientHttpResponse转换为JavaBean，主要用在RestTemplate当中，去完成类型的转换
 *
 * @see RestTemplate
 */
@FunctionalInterface
interface ResponseExtractor<T> {

    /**
     * 从ClientHttpResponse提取到ResponseBody的数据
     *
     * @param response response
     * @return 提取到的数据(如果没有提取到return null)
     */
    fun extractData(response: ClientHttpResponse): T?
}