package com.wanna.framework.web.client

import com.wanna.framework.web.bind.RequestMethod
import java.net.URI

/**
 * 封装了Rest相关的操作，可以使用它去完成Http请求的发送与响应的处理，具体实现见RestTemplate
 *
 * @see RestTemplate
 */
interface RestOperations {

    /**
     * 使用GET方式去执行一次Http请求
     *
     * @param url url
     * @param responseType 响应类型
     * @param uriVariables 请求参数列表
     * @return Http请求返回的ResponseBody转换成为JavaBean(如果转换失败，return null)
     */
    fun <T> getForObject(url: String, responseType: Class<T>, uriVariables: Map<String, String>): T?

    /**
     * 使用POST请求的方式去执行一次Http请求
     *
     * @param url url
     * @param responseType 响应类型
     * @param uriVariables 请求参数列表
     * @return Http请求返回的ResponseBody转换成为JavaBean(如果转换失败，return null)
     */
    fun <T> postForObject(url: String, responseType: Class<T>, uriVariables: Map<String, String>): T?

    /**
     * 执行一次Http请求
     *
     * @param url url(包含参数)
     * @param method 请求方式(GET/POST/...)
     * @param requestCallback 对请求去进行处理的回调函数(可以为null)
     * @param responseExtractor 对响应去进行转成JavaBean的提取器(如果不指定的话，返回值为null)
     * @return 转换成为的JavaBean(如果没有给定responseExtractor，return null; 如果responseExtractor解析结果为null，return null)
     */
    fun <T> execute(
        url: URI, method: RequestMethod, requestCallback: RequestCallback?, responseExtractor: ResponseExtractor<T>?
    ): T?
}