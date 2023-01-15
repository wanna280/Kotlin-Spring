package com.wanna.framework.web.method.annotation

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.web.bind.ServerRequestBindingException
import com.wanna.framework.web.bind.annotation.RequestHeader
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
     * @return 当前的方法参数解析器是否支持处理这样的方法参数？如果标注了`@RequestHeader`注解则支持去进行处理, return true; 否则return false
     */
    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.getAnnotation(RequestHeader::class.java) != null

    /**
     * 给定headerName从request当中去解析requestHeader的值
     *
     * @param name headerName
     * @param parameter 方法参数
     * @param webRequest NativeWebRequest(request and response)
     * @return 从request当中解析到的headerValue
     */
    override fun resolveName(name: String, parameter: MethodParameter, webRequest: NativeWebRequest): Any? {
        val request = webRequest.getNativeRequest(HttpServerRequest::class.java)
        return request.getHeader(name)
    }

    /**
     * 创建@RequestHeader的NamedValueInfo
     *
     * @param parameter 方法参数
     * @return 根据@RequestHeader注解, 解析出来的NamedValueInfo对象
     */
    override fun createNamedValueInfo(parameter: MethodParameter): NamedValueInfo {
        val requestHeader = parameter.getAnnotation(RequestHeader::class.java)!!
        return RequestHeaderNamedValueInfo(requestHeader.name, requestHeader.required, requestHeader.defaultValue)
    }

    override fun handleMissingValue(name: String, parameter: MethodParameter) {
        throw ServerRequestBindingException("在绑定[${parameter.getExecutable()}]的[${parameter.getParameter()}]时遇到了, 缺失[$name]对应的HttpHeader")
    }

    /**
     * 这是基于@RequestHeader的NamedValueInfo, 维护name/required/defaultValue等信息
     */
    private class RequestHeaderNamedValueInfo(name: String, required: Boolean, defaultValue: String) :
        NamedValueInfo(name, required, defaultValue)
}