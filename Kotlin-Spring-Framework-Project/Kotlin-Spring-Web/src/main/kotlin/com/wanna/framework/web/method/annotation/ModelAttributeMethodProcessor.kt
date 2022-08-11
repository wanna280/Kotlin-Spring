package com.wanna.framework.web.method.annotation

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.core.util.BeanUtils
import com.wanna.framework.web.bind.WebDataBinder
import com.wanna.framework.web.bind.WebRequestDataBinder
import com.wanna.framework.web.bind.annotation.ModelAttribute
import com.wanna.framework.web.bind.support.WebDataBinderFactory
import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.method.support.HandlerMethodArgumentResolver
import com.wanna.framework.web.method.support.HandlerMethodReturnValueHandler
import com.wanna.framework.web.method.support.ModelAndViewContainer

/**
 * 它是一个ModelAttribute的方法处理器，支持处理ModelAttribute的方法；同时也支持将请求参数列表转换为JavaBean
 *
 * * 1.如果方法参数上标注了ModelAttribute(或者方法参数不是一个简单属性的话)，那么可以将请求参数转为JavaBean
 * * 2.如果方法上标注了ModelAttribute(获取方法返回值不是一个简单属性的话)，那么将该数据放入到Model当中
 *
 * @param annotationNotRequired 是否必须标注ModelAttribute注解才支持去进行处理？(默认为true，代表没有注解也可以支持处理)
 */
open class ModelAttributeMethodProcessor(private val annotationNotRequired: Boolean = true) :
    HandlerMethodReturnValueHandler,
    HandlerMethodArgumentResolver {

    /**
     * 它是否支持处理这样的参数？只要它有ModelAttribute方法，或者它不是一个简单类型(并且注解不是必要的)，那么就支持去进行处理
     *
     * @param parameter 要去进行匹配的目标参数
     * @return 只要它有ModelAttribute方法，或者它不是一个基础类型，return true；否则return false
     */
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasMethodAnnotation(ModelAttribute::class.java) ||
                (annotationNotRequired && !BeanUtils.isSimpleProperty(parameter.getParameterType()))
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        webRequest: NativeWebRequest,
        mavContainer: ModelAndViewContainer?,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val parameterType = parameter.getParameterType()
        val instance = parameterType.getDeclaredConstructor().newInstance()
        val binder = binderFactory?.createBinder(webRequest, instance, parameter.getParameterName()!!)
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
        return parameter.getAnnotation(ModelAttribute::class.java) != null ||
                (annotationNotRequired && !BeanUtils.isSimpleProperty(parameter.getParameterType()))
    }

    /**
     * 处理返回值，如果返回值不是一个简单属性的话，将返回值放入到Model数据当中
     *
     * @param returnType returnType
     * @param returnValue returnValue
     * @param webRequest webRequest
     * @param mavContainer mavContainer
     */
    override fun handleReturnValue(
        returnValue: Any?,
        webRequest: NativeWebRequest,
        returnType: MethodParameter,
        mavContainer: ModelAndViewContainer
    ) {
        if (returnValue != null) {
            val returnValueName = ModelFactory.getNameForReturnValue(returnValue, returnType)
            mavContainer.addAttribute(returnValueName, returnValue)
        }
    }
}