package com.wanna.framework.web.method.support

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.web.context.request.NativeWebRequest

/**
 * 基于视图名的返回值处理器, 如果返回值是字符串的话, 支持将返回值设置成为视图名, 设置到ModelAndViewContainer当中
 *
 * @see HandlerMethodReturnValueHandler
 */
open class ViewNameMethodReturnValueHandler : HandlerMethodReturnValueHandler {

    /**
     * 它支持处理字符串类型的返回值, 负责将字符串类型的返回值解析成为视图名
     *
     * @param parameter 方法的返回值类型封装的MethodParameter
     * @return 能否支持处理该返回值类型? 只要返回值是字符串就支持
     */
    override fun supportsReturnType(parameter: MethodParameter): Boolean {
        return ClassUtils.isAssignFrom(CharSequence::class.java, parameter.getParameterType())
    }

    /**
     * 处理返回值, 当返回值是字符串时, 才去进行解析
     *
     * @param returnValue 执行Handler方法的最终返回值
     * @param webRequest NativeWebRequest
     * @param returnType 返回值类型
     * @param mavContainer ModelAndView的容器(存放视图和模型的数据)
     */
    override fun handleReturnValue(
        returnValue: Any?,
        webRequest: NativeWebRequest,
        returnType: MethodParameter,
        mavContainer: ModelAndViewContainer
    ) {
        if (returnValue is CharSequence) {
            val viewName = returnValue.toString()
            mavContainer.view = viewName
            if (isRedirectView(viewName)) {
                mavContainer.redirectModelScenario = true
            }
        }
    }

    /**
     * 它是否是一个重定向的是视图
     *
     * @param viewName viewName
     * @return 如果viewName以"redirect:", 则说明它是重定向视图, return true; 否则return false
     */
    protected open fun isRedirectView(viewName: String): Boolean {
        return viewName.startsWith("redirect:")
    }
}