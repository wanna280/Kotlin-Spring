package com.wanna.framework.web.method.annotation

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.core.util.BeanUtils
import com.wanna.framework.web.bind.WebDataBinder
import com.wanna.framework.web.bind.WebRequestDataBinder
import com.wanna.framework.web.bind.support.WebDataBinderFactory
import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.method.support.HandlerMethodArgumentResolver
import com.wanna.framework.web.method.support.HandlerMethodReturnValueHandler
import com.wanna.framework.web.method.support.ModelAndViewContainer

/**
 * 它是一个ModelAttribute的方法处理器，支持处理ModelAttribute的方法；同事也支持将请求参数列表转换为JavaBean
 */
open class ModelAttributeMethodProcessor : HandlerMethodReturnValueHandler, HandlerMethodArgumentResolver {

    /**
     * 它是否支持处理这样的参数？只要它有ModelAttribute方法，或者它不是一个基础类型，那么就支持去进行处理
     *
     * @param parameter 要去进行匹配的目标参数
     * @return 只要它有ModelAttribute方法，或者它不是一个基础类型，return true；否则return false
     */
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.getAnnotation(ModelAttribute::class.java) != null || !BeanUtils.isSimpleProperty(parameter.getParameterType())
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        webRequest: NativeWebRequest,
        mavContainer: ModelAndViewContainer?,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val parameterType = parameter.getParameterType()
        val instance = parameterType.getDeclaredConstructor().newInstance()
        val binder =
            binderFactory?.createBinder(webRequest, instance, parameter.getParameterName()!!)
                ?: throw IllegalStateException("在要处理ModelAttribute时BinderFactory不能为空")

        if (binder.getTarget() != null) {
            bindRequestParameters(binder, webRequest)
        }
        return binder.convertIfNecessary(binder.getTarget(), parameterType)
    }

    /**
     * 绑定请求参数
     *
     * @param binder binder
     * @param webRequest request
     */
    open fun bindRequestParameters(binder: WebDataBinder, webRequest: NativeWebRequest) {
        (binder as WebRequestDataBinder).bind(webRequest)
    }

    override fun supportsReturnType(parameter: MethodParameter): Boolean {
        return false
    }

    override fun handleReturnValue(
        returnValue: Any?,
        webRequest: NativeWebRequest,
        returnType: MethodParameter,
        mavContainer: ModelAndViewContainer
    ) {
        TODO("Not yet implemented")
    }
}