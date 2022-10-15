package com.wanna.framework.web.method.annotation

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.web.HandlerMapping
import com.wanna.framework.web.bind.annotation.PathVariable
import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.server.HttpServerRequest

/**
 * 基于路径变量的参数解析器
 */
open class PathVariableHandlerMethodArgumentResolver : AbstractNamedValueMethodArgumentResolver() {
    override fun resolveName(name: String, webRequest: NativeWebRequest): Any? {
        val request = webRequest.getNativeRequest(HttpServerRequest::class.java)
        val uriTemplates = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)
        if (uriTemplates != null && uriTemplates is Map<*, *>) {
            return uriTemplates[name]
        }
        return null
    }

    override fun createNamedValueInfo(parameter: MethodParameter): NamedValueInfo {
        val pathVariable =
            parameter.getAnnotation(PathVariable::class.java) ?: throw IllegalStateException("该参数上没有@PathVariable")
        return PathVariableNamedValueInfo(pathVariable.value)
    }

    override fun supportsParameter(parameter: MethodParameter) =
        parameter.getAnnotation(PathVariable::class.java) != null
                && !ClassUtils.isAssignFrom(Map::class.java, parameter.getParameterType())

    private class PathVariableNamedValueInfo(name: String) : NamedValueInfo(name, true, null)
}