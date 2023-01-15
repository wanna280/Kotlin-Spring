package com.wanna.framework.web.method.support

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.handler.ModelAndView
import com.wanna.framework.web.ui.SmartView

/**
 * ModelAndView的返回值解析器, 支持去处理返回值为ModelAndView的方法, 将ModelAndView当中的数据转移到ModelAndView的容器(ModelAndViewContainer)当中
 *
 * @see ModelAndView
 * @see HandlerMethodReturnValueHandler
 */
open class ModelAndViewMethodReturnValueHandler : HandlerMethodReturnValueHandler {

    /**
     * 是否处理该返回值？只要返回值类型是ModelAndView就支持
     *
     * @param parameter 返回值类型封装成为的MethodParameter
     */
    override fun supportsReturnType(parameter: MethodParameter): Boolean {
        return ClassUtils.isAssignFrom(ModelAndView::class.java, parameter.getParameterType())
    }

    /**
     * 处理ModelAndView的方法返回值
     *
     * @param returnValue 方法的执行返回值
     * @param webRequest NativeWebRequest
     * @param returnType 方法的返回值类型
     * @param mavContainer ModelAndView的容器
     */
    override fun handleReturnValue(
        returnValue: Any?,
        webRequest: NativeWebRequest,
        returnType: MethodParameter,
        mavContainer: ModelAndViewContainer
    ) {
        val modelAndView = returnValue as ModelAndView
        mavContainer.view = modelAndView.view

        val view = modelAndView.view
        if (view is String) {
            if (isRedirectView(view)) {
                mavContainer.redirectModelScenario = true
            }
        } else {
            if (view is SmartView) {
                mavContainer.redirectModelScenario = view.isRedirectView()
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