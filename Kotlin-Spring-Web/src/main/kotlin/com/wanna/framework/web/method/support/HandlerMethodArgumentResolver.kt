package com.wanna.framework.web.method.support

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.web.bind.support.WebDataBinderFactory
import com.wanna.framework.web.context.request.NativeWebRequest

/**
 * 这是一个HandlerMethod的参数解析器，负责解析HandlerMethod的参数；
 * (1)supportsParameter方法负责去判断当前参数解析器是否支持去处理这样的方法参数；
 * (2)如果supportsParameter方法return true，那么需要交由resolveArgument去完成最终的方法参数的解析
 *
 */
interface HandlerMethodArgumentResolver {

    /**
     * 是否支持解析当前方法参数
     *
     * @param parameter 方法参数
     * @return 是否支持处理？支持处理return true；否则return false
     */
    fun supportsParameter(parameter: MethodParameter): Boolean

    /**
     * 如果支持处理当前的方法参数的话，需要交由这个方法去完成目标参数的解析工作
     *
     * @param parameter 方法参数
     * @param webRequest NativeWebRequest(request and response)
     * @param mavContainer ModelAndView的Container
     * @param binderFactory WebDataBinderFactory，提供类型转换
     * @return 解析到的参数，有可能为null
     */
    fun resolveArgument(
        parameter: MethodParameter,
        webRequest: NativeWebRequest,
        mavContainer: ModelAndViewContainer?,
        binderFactory: WebDataBinderFactory?
    ): Any?
}