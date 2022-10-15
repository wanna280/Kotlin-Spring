package com.wanna.framework.web.method.annotation

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.web.bind.support.WebDataBinderFactory
import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.method.support.HandlerMethodArgumentResolver
import com.wanna.framework.web.method.support.ModelAndViewContainer
import com.wanna.framework.web.server.HttpServerResponse
import java.io.OutputStream

/**
 * HttpServerResponse的参数解析器，负责处理HandlerMethod的参数当中，跟HttpServerResponse相关的参数
 */
open class ServerResponseMethodArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return ClassUtils.isAssignFrom(
            HttpServerResponse::class.java,
            parameter.getParameterType()
        ) || ClassUtils.isAssignFrom(OutputStream::class.java, parameter.getParameterType())
    }

    /**
     * 解析HttpServerResponse参数
     */
    override fun resolveArgument(
        parameter: MethodParameter, webRequest: NativeWebRequest, mavContainer: ModelAndViewContainer?,binderFactory: WebDataBinderFactory?
    ): Any? {
        val response = webRequest.getNativeResponse(HttpServerResponse::class.java)
        if (ClassUtils.isAssignFrom(HttpServerResponse::class.java, parameter.getParameterType())) {
            return response
        }
        if (ClassUtils.isAssignFrom(OutputStream::class.java, parameter.getParameterType())) {
            return response?.getOutputStream()
        }
        return null
    }
}