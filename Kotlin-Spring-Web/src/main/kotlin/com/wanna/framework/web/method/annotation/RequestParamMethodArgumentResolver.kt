package com.wanna.framework.web.method.annotation

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.server.HttpServerRequest

/**
 * 这是一个处理@RequestParam注解的HandlerMethod的参数解析器
 *
 * @see RequestParam
 */
open class RequestParamMethodArgumentResolver : AbstractNamedValueMethodArgumentResolver() {

    /**
     * 是否支持处理这样的参数？只要参数上标注了@RequestParam注解，就支持去进行处理
     *
     * @param parameter 方法参数
     * @return 是否支持去进行处理
     */
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        val requestParam = parameter.getAnnotation(RequestParam::class.java)
        if (requestParam != null) {
            return true
        }
        return false
    }

    /**
     * 给定paramName，需要去计算该paramName在请求参数当中的具体的值
     *
     * @param name paramName
     * @param webRequest NativeWebRequest(request and response)
     * @return 从header当中获取到的参数的值
     */
    override fun resolveName(name: String, webRequest: NativeWebRequest): Any? {
        val request = webRequest.getNativeRequest(HttpServerRequest::class.java)
        return request.getParam(name)
    }

    /**
     * 构建RequestParam的NamedValueInfo
     *
     * @param parameter 方法参数
     * @return 根据该方法参数当中的@RequestParam注解，去解析成为NamedValueInfo信息
     */
    override fun createNamedValueInfo(parameter: MethodParameter): NamedValueInfo {
        val requestParam = parameter.getAnnotation(RequestParam::class.java)!!
        return RequestParamNamedValueInfo(requestParam.name, requestParam.required, requestParam.defaultValue)
    }

    /**
     * 针对于RequestParam的NamedValueInfo，负责包装name/required/defaultValue等信息
     */
    private class RequestParamNamedValueInfo(name: String, required: Boolean, defaultValue: String) :
        NamedValueInfo(name, required, defaultValue)
}