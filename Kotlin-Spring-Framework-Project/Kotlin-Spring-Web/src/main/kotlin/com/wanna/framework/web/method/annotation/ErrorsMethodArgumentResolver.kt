package com.wanna.framework.web.method.annotation

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.core.util.ClassUtils
import com.wanna.framework.validation.Errors
import com.wanna.framework.web.bind.support.WebDataBinderFactory
import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.method.support.HandlerMethodArgumentResolver
import com.wanna.framework.web.method.support.ModelAndViewContainer

/**
 * 处理Errors的方法的参数解析器，例如BindingResult就是通过这个参数解析器去进行的解析工作
 *
 * @see HandlerMethodArgumentResolver
 * @see Errors
 */
open class ErrorsMethodArgumentResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter) =
        ClassUtils.isAssignFrom(Errors::class.java, parameter.getParameterType())

    override fun resolveArgument(
        parameter: MethodParameter,
        webRequest: NativeWebRequest,
        mavContainer: ModelAndViewContainer?,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        return null
    }
}