package com.wanna.framework.web.method.annotation

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.core.util.ClassUtils
import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.method.support.HandlerMethodArgumentResolver
import com.wanna.framework.web.method.support.ModelAndViewContainer
import com.wanna.framework.web.server.HttpServerRequest
import java.io.InputStream

/**
 * HttpServerRequest的参数解析器，负责处理HandlerMethod的参数当中，跟HttpServerRequest相关的参数
 */
open class ServerRequestMethodArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return ClassUtils.isAssignFrom(HttpServerRequest::class.java, parameter.getParameterType()) ||
                ClassUtils.isAssignFrom(InputStream::class.java, parameter.getParameterType())
    }

    override fun resolveArgument(parameter: MethodParameter, webRequest: NativeWebRequest,mavContainer: ModelAndViewContainer?): Any? {
        val request = webRequest.getNativeRequest(HttpServerRequest::class.java)
        if (ClassUtils.isAssignFrom(HttpServerRequest::class.java, parameter.getParameterType())) {
            return request
        }
        if (ClassUtils.isAssignFrom(InputStream::class.java, parameter.getParameterType())) {
            return request.getInputStream()
        }
        return null
    }
}