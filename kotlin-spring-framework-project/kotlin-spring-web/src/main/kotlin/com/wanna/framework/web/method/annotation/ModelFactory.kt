package com.wanna.framework.web.method.annotation

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.util.StringUtils
import com.wanna.framework.web.bind.annotation.ModelAttribute
import com.wanna.framework.web.bind.support.WebDataBinderFactory
import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.method.HandlerMethod
import com.wanna.framework.web.method.support.InvocableHandlerMethod
import com.wanna.framework.web.method.support.ModelAndViewContainer

/**
 * ModelFactory, 提供将@ModelAttribute方法的执行过程当中产生的Model数据, 全部合并到ModelAndViewContainer当中
 *
 * @param binderFactory BinderFactory
 * @param handlerMethods @ModelAttribute的HandlerMethod列表
 */
class ModelFactory(
    private val handlerMethods: List<InvocableHandlerMethod>,
    private val binderFactory: WebDataBinderFactory
) {

    /**
     * 初始化Model, apply所有@ModelAttribute的HandlerMethod
     *
     * @param webRequest (request&response)
     * @param mavContainer ModelAndViewContainer
     */
    fun initModel(webRequest: NativeWebRequest, mavContainer: ModelAndViewContainer, handlerMethod: HandlerMethod) {
        initModelAttributeMethods(webRequest, mavContainer)
    }

    /**
     * 执行所有的@ModelAttribute方法
     *
     * @param webRequest (request&response)
     * @param mavContainer ModelAndViewContainer
     */
    private fun initModelAttributeMethods(webRequest: NativeWebRequest, mavContainer: ModelAndViewContainer) {
        for (modelMethod in handlerMethods) {
            modelMethod.binderFactory = binderFactory
            // 执行目标方法
            val returnValue = modelMethod.invokeForRequest(webRequest, mavContainer)
            if (modelMethod.isVoid()) {
                continue
            }
            // 获取returnValueName(如果有ModelAttribute注解的属性的话, 从里面去进行获取; 如果没有的话, 那么使用类型的首字母小写)
            val returnValueName = getNameForReturnValue(returnValue, modelMethod.getReturnValueType(returnValue))
            if (!mavContainer.containsAttribute(returnValueName)) {
                // 将值转移到ModelAndViewContainer当中
                mavContainer.addAttribute(returnValueName, returnValue)
            }
        }
    }

    companion object {
        /**
         * 获取returnValueName(如果有ModelAttribute注解的属性的话, 从里面去进行获取; 如果没有的话, 那么使用类型的首字母小写)
         *
         * @param returnValue returnValue
         * @param returnType returnType
         * @return returnValueName
         */
        @JvmStatic
        fun getNameForReturnValue(returnValue: Any?, returnType: MethodParameter): String {
            val modelAttribute = returnType.getAnnotation(ModelAttribute::class.java)
            val name = modelAttribute?.name
            if (StringUtils.hasText(name)) {
                return name!!
            }
            val simpleName = returnType.getParameterType().simpleName
            return simpleName[0].lowercase() + simpleName.substring(1)
        }
    }
}