package com.wanna.framework.web.method.annotation

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.server.HttpServerRequest

/**
 * 这是一个支持处理@RequestHeader注解的HandlerMethod的参数解析器
 *
 * @see RequestHeader
 */
open class RequestHeaderMethodArgumentResolver : AbstractNamedValueMethodArgumentResolver() {

    /**
     * 是否支持去处理当前类型的参数？
     *
     * @param parameter 方法参数
     * @return 是否支持处理？
     */
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        val requestHeader = parameter.getAnnotation(RequestHeader::class.java)
        if (requestHeader != null) {
            return true
        }
        return false
    }

    /**
     * 给定headerName去解析requestHeader的值
     *
     * @param name headerName
     * @param webRequest NativeWebRequest(request and response)
     * @return 从request当中解析到的headerValue
     */
    override fun resolveName(name: String, webRequest: NativeWebRequest): Any? {
        val request = webRequest.getNativeRequest(HttpServerRequest::class.java)
        return request.getHeader(name)
    }

    /**
     * 创建@RequestHeader的NamedValueInfo
     *
     * @param 方法参数
     * @return 根据@RequestHeader注解，解析出来的NamedValueInfo对象
     */
    override fun createNamedValueInfo(parameter: MethodParameter): NamedValueInfo {
        val requestHeader = parameter.getAnnotation(RequestHeader::class.java)!!
        return RequestHeaderNamedValueInfo(requestHeader.name, requestHeader.required, requestHeader.defaultValue)
    }

    /**
     * 这是基于@RequestHeader的NamedValueInfo，维护name/required/defaultValue等信息
     */
    private class RequestHeaderNamedValueInfo(name: String, required: Boolean, defaultValue: String) :
        NamedValueInfo(name, required, defaultValue)
}