package com.wanna.framework.web.method.annotation

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.core.util.ClassUtils
import com.wanna.framework.web.bind.support.WebDataBinderFactory
import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.method.support.HandlerMethodArgumentResolver
import com.wanna.framework.web.method.support.HandlerMethodReturnValueHandler
import com.wanna.framework.web.method.support.ModelAndViewContainer

/**
 * Map的方法处理器
 * * 1.如果方法需要Map参数的话，那么注入ModelAndViewContainer的Model数据
 * * 2.如果方法的返回值是Map的话，那么需要将Model当中的数据全部转移到ModelAndViewContainer当中
 *
 * Note: Model参数和Map参数等价
 *
 * @see ModelAndViewContainer
 * @see ModelMethodProcessor
 */
open class MapMethodProcessor : HandlerMethodArgumentResolver, HandlerMethodReturnValueHandler {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return ClassUtils.isAssignFrom(
            Map::class.java,
            parameter.getParameterType()
        ) && parameter.getAnnotations().isEmpty()
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        webRequest: NativeWebRequest,
        mavContainer: ModelAndViewContainer?, binderFactory: WebDataBinderFactory?
    ): Any? {
        return mavContainer?.getModel() ?: throw IllegalStateException("在需要暴露Model的情况下，ModelAndView不能为null")
    }

    override fun supportsReturnType(parameter: MethodParameter): Boolean {
        return ClassUtils.isAssignFrom(Map::class.java, parameter.getParameterType())
    }

    override fun handleReturnValue(
        returnValue: Any?,
        webRequest: NativeWebRequest,
        returnType: MethodParameter,
        mavContainer: ModelAndViewContainer
    ) {
        if (returnValue == null) {
            return
            // 如果它的返回值是Map的话，需要将Model当中的数据全部添加到ModelAndViewContainer当中
        } else if (returnValue is Map<*, *>) {
            @Suppress("UNCHECKED_CAST")
            mavContainer.getModel().putAll(returnValue as Map<out String, Any>)
        } else {
            throw UnsupportedOperationException("期待的返回值类型是Model，但是给定的类型[${returnType.getParameterType()}]不是Model类型")
        }
    }
}