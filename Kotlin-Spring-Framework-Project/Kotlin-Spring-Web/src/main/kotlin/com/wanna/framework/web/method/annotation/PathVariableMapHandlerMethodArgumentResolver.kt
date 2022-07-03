package com.wanna.framework.web.method.annotation

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.core.util.ClassUtils
import com.wanna.framework.web.HandlerMapping
import com.wanna.framework.web.bind.annotation.PathVariable
import com.wanna.framework.web.bind.support.WebDataBinderFactory
import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.method.support.HandlerMethodArgumentResolver
import com.wanna.framework.web.method.support.ModelAndViewContainer
import com.wanna.framework.web.server.HttpServerRequest

/**
 * 处理@PathVariable为Map的情况
 */
class PathVariableMapHandlerMethodArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter) =
        parameter.getAnnotation(PathVariable::class.java) != null
                && ClassUtils.isAssignFrom(Map::class.java, parameter.getParameterType())

    override fun resolveArgument(
        parameter: MethodParameter,
        webRequest: NativeWebRequest,
        mavContainer: ModelAndViewContainer?,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        return webRequest.getNativeRequest(HttpServerRequest::class.java)
            .getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)
    }
}