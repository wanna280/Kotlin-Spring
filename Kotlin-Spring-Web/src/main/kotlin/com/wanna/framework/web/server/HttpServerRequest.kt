package com.wanna.framework.web.server

import com.wanna.framework.web.bind.RequestMethod
import com.wanna.framework.web.http.HttpHeaders
import java.io.InputStream

/**
 * HttpServerRequest
 */
interface HttpServerRequest {

    companion object {
        const val COMMA = "; "
    }

    /**
     * 获取request的输入流，可以从输出流当中获取RequestBody
     *
     * @return RequestBody的输入流
     */
    fun getInputStream(): InputStream

    /**
     * 设置request的具体的参数(如果之前已经有该参数了，那么直接去进行替换)
     *
     * @param name paramName
     * @param value paramValue(为null时代表移除)
     */
    fun setParam(name: String, value: String?)

    /**
     * 添加参数，如果value为空的话，移除该name的param
     *
     * @param name paramName
     * @param value paramValue(为null时代表移除)
     */
    fun addParam(name: String, value: String?)

    /**
     * 根据name获取param
     *
     * @param name paramName
     * @return 给定paramName获取到的参数列表(如果存在有多个param，那么使用"; "去进行分割)
     */
    fun getParam(name: String): String?

    /**
     * 根据name移除一个参数
     *
     * @param name paramName
     */
    fun removeParam(name: String)

    /**
     * 添加Header，如果value为空的话，标识移除该name的header
     *
     * @param name headerName
     * @param value headerValue
     */
    fun addHeader(name: String, value: String?)

    /**
     * 设置某个Header的值
     *
     * @param name headerName
     * @param value headerValue
     */
    fun setHeader(name: String, value: String)

    /**
     * 根据name去获取到headerValue
     *
     * @param name headerName
     * @return 根据name去获取到的headerValue(如果该header存在有多个值，那么使用"; "去进行分割)
     */
    fun getHeader(name: String): String?

    /**
     * 获取当前request的HttpHeaders
     *
     * @return HttpHeaders of request
     */
    fun getHeaders(): HttpHeaders

    /**
     * 获取当前请求的uri(包含url和query参数)
     *
     * @return uri of request
     */
    fun getUri(): String

    /**
     * 获取当前请求的url(不包含query参数)
     *
     * @return url of request
     */
    fun getUrl(): String

    /**
     * 获取当前请求的请求方式
     *
     * @return request method of request
     */
    fun getMethod(): RequestMethod
}