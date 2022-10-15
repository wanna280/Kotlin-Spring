package com.wanna.framework.web.method.support

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.web.context.request.NativeWebRequest

/**
 * 它是一个负责去处理HandlerMethod的返回值的处理器
 *
 * @see HandlerMethodArgumentResolver
 */
interface HandlerMethodReturnValueHandler {

    /**
     * 是否支持处理这样的返回值？
     *
     * @param parameter 方法返回值封装的MethodParameter
     * @return 是否支持处理？支持return true；不然return false
     */
    fun supportsReturnType(parameter: MethodParameter): Boolean

    /**
     * 如果它支持处理返回值的话，应该交给这个方法去完成返回值的最终处理工作
     *
     * @param returnValue 方法执行的返回值
     * @param webRequest NativeWebRequest(request and response)
     * @param returnType 返回值类型封装的MethodParameter
     */
    fun handleReturnValue(
        returnValue: Any?,
        webRequest: NativeWebRequest,
        returnType: MethodParameter,
        mavContainer: ModelAndViewContainer
    )
}