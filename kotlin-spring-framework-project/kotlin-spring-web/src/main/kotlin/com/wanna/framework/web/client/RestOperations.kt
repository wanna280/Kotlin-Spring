package com.wanna.framework.web.client

import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.bind.annotation.RequestMethod
import com.wanna.framework.web.http.HttpEntity
import com.wanna.framework.web.http.ResponseEntity
import java.net.URI

/**
 * 封装了Rest相关的操作, 可以使用它去完成Http请求的发送与响应的处理, 具体实现见RestTemplate
 *
 * @see RestTemplate
 */
interface RestOperations {

    /**
     * 使用GET方式去执行一次Http请求, 将ResponseBody转换为JavaBean
     *
     * @param url url
     * @param responseType 响应类型
     * @param uriVariables 请求参数列表
     * @return Http请求返回的ResponseBody转换成为JavaBean(如果转换失败, return null)
     */
    @Nullable
    fun <T : Any> getForObject(url: String, responseType: Class<T>, uriVariables: Map<String, String>): T?

    /**
     * 使用GET方式去执行一次Http请求, 将ResponseBody转换为ResponseEntity(headers&status&body)
     *
     * @param url url
     * @param responseType 响应类型
     * @param uriVariables 请求参数列表
     * @return Http请求返回的ResponseBody转换成为包含有JavaBean的ResponseEntity(如果转换失败, return null)
     */
    @Nullable
    fun <T : Any> getForEntity(
        url: String,
        responseType: Class<T>,
        uriVariables: Map<String, String>
    ): ResponseEntity<T>?

    /**
     * 使用POST请求的方式去执行一次Http请求, 将ResponseBody转换为JavaBean
     *
     * @param url url
     * @param responseType 响应类型
     * @param uriVariables 请求参数列表
     * @return Http请求返回的ResponseBody转换成为JavaBean(如果转换失败, return null)
     */
    @Nullable
    fun <T : Any> postForObject(url: String, responseType: Class<T>, uriVariables: Map<String, String>): T?

    /**
     * 使用POST请求的方式去执行一次Http请求, 将ResponseBody转换为ResponseEntity(headers&status&body)
     *
     * Note: 支持携带RequestBody
     *
     * @param url url
     * @param responseType 响应类型
     * @param uriVariables 请求参数列表
     * @param requestBody RequestBody(可以为null, 也可以是[HttpEntity], 如果需要自定义HttpHeaders, 可以使用到[HttpEntity])
     * @return Http请求返回的ResponseBody转换成为JavaBean(如果转换失败, return null)
     */
    @Nullable
    fun <T : Any> postForObject(
        url: String,
        @Nullable requestBody: Any?,
        responseType: Class<T>,
        uriVariables: Map<String, String>
    ): T?

    /**
     * 使用POST请求的方式去执行一次Http请求, 将ResponseBody转换为JavaBean
     *
     * @param url url
     * @param responseType 响应类型
     * @param uriVariables 请求参数列表
     * @return Http请求返回的ResponseBody转换成为包含有JavaBean的ResponseEntity(如果转换失败, return null)
     */
    @Nullable
    fun <T : Any> postForEntity(
        url: String,
        responseType: Class<T>,
        uriVariables: Map<String, String>
    ): ResponseEntity<T>?

    /**
     * 使用POST请求的方式去执行一次Http请求, 将ResponseBody转换为ResponseEntity(headers&status&body)
     *
     * Note: 支持携带RequestBody
     *
     * @param url url
     * @param responseType 响应类型
     * @param uriVariables 请求参数列表
     * @param requestBody RequestBody(可以为null, 也可以是[HttpEntity], 如果需要自定义HttpHeaders, 可以使用到[HttpEntity])
     * @return Http请求返回的ResponseBody转换成为包含有JavaBean的ResponseEntity(如果转换失败, return null)
     */
    @Nullable
    fun <T : Any> postForEntity(
        url: String,
        @Nullable requestBody: Any?,
        responseType: Class<T>,
        uriVariables: Map<String, String>
    ): ResponseEntity<T>?


    /**
     * 执行一次Http请求
     *
     * @param uri 请求的url(包含参数)
     * @param method 请求方式(GET/POST/...)
     * @param requestCallback 对请求去进行处理的回调函数(可以为null)
     * @param responseExtractor 对响应去进行转成JavaBean的提取器(如果不指定的话, 返回值为null)
     * @return 转换成为的JavaBean(如果没有给定responseExtractor, return null; 如果responseExtractor解析结果为null, return null)
     */
    @Nullable
    fun <T : Any> execute(
        uri: URI,
        method: RequestMethod,
        @Nullable requestCallback: RequestCallback?,
        @Nullable responseExtractor: ResponseExtractor<T>?
    ): T?
}