package com.wanna.framework.web.method.annotation

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.web.bind.support.WebDataBinderFactory
import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.method.support.HandlerMethodArgumentResolver
import com.wanna.framework.web.method.support.HandlerMethodReturnValueHandler
import com.wanna.framework.web.method.support.ModelAndViewContainer
import com.wanna.framework.web.ui.Model

/**
 * Model方法的处理器，负责处理方法当中的Model类型的参数，以及Model类型的方法返回值；
 *
 * * 1.如果方法需要Model参数的话，那么注入ModelAndViewContainer的Model数据
 * * 2.如果方法的返回值是Model的话，那么需要将Model当中的数据全部转移到ModelAndViewContainer当中
 *
 * Note: Model参数和Map参数等价
 *
 * @see Model
 * @see ModelAndViewContainer
 * @see MapMethodProcessor
 */
open class ModelMethodProcessor : HandlerMethodArgumentResolver, HandlerMethodReturnValueHandler {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return ClassUtils.isAssignFrom(Model::class.java, parameter.getParameterType())
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        webRequest: NativeWebRequest,
        mavContainer: ModelAndViewContainer?,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        return mavContainer?.getModel() ?: throw IllegalStateException("在需要暴露Model的情况下，ModelAndView不能为null")
    }

    override fun supportsReturnType(parameter: MethodParameter): Boolean {
        return ClassUtils.isAssignFrom(Model::class.java, parameter.getParameterType())
    }

    override fun handleReturnValue(
        returnValue: Any?,
        webRequest: NativeWebRequest,
        returnType: MethodParameter,
        mavContainer: ModelAndViewContainer
    ) {
        if (returnValue == null) {
            return
            // 如果它的返回值是Model的话，需要将Model当中的数据全部添加到ModelAndViewContainer当中
        } else if (returnValue is Model) {
            mavContainer.getModel().putAll(returnValue.asMap())
        } else {
            throw UnsupportedOperationException("期待的返回值类型是Model，但是给定的类型[${returnType.getParameterType()}]不是Model类型")
        }
    }
}