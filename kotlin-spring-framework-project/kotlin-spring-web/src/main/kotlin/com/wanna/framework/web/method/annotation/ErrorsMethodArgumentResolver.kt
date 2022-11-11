package com.wanna.framework.web.method.annotation

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.validation.BindingResult.Companion.MODEL_KEY_PREFIX
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

    /**
     * 支持哪些类型的参数？支持去处理Errors/BindingResult类型的方法参数(BindingResult是Errors的子接口)
     *
     * @param parameter 方法参数
     * @return 如果它是Errors的子类(包括BindingResult的子类)，那么就支持去进行处理
     */
    override fun supportsParameter(parameter: MethodParameter) =
        ClassUtils.isAssignFrom(Errors::class.java, parameter.getParameterType())

    /**
     * 解析Errors类型的参数，需要拿到ModelAndViewContainer当中的最后一个属性数据，把它去去进行返回
     *
     * @param parameter 方法参数
     * @param webRequest WebRequest
     * @param mavContainer ModelAndViewContainer
     * @param binderFactory WebDataBinderFactory
     *
     * @throws IllegalStateException 如果ModelAndViewContainer为空，或者当前参数之前没有要去进行检验的@ModelAttribute/@RequestBody/@RequestPart参数
     */
    override fun resolveArgument(
        parameter: MethodParameter,
        webRequest: NativeWebRequest,
        mavContainer: ModelAndViewContainer?,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        mavContainer ?: throw IllegalStateException("处理Errors的参数时ModelAndViewContainer不能为空")
        val model = mavContainer.getModel()
        val lastKey = model.keys.lastOrNull()
        // 断言这个key应该是以BindingResult的ModelKeyPrefix开头的
        // 如果不是以这个作为前缀的话，说明发生了错误的情况(它前面没有解析相关的参数，因此没有拿到BindingResult数据)
        if (lastKey != null && lastKey.startsWith(MODEL_KEY_PREFIX)) {
            return model[lastKey]
        }
        throw IllegalStateException("一个Errors/BindingResult参数，只能紧跟在@ModelAttribute/@RequestBody/@RequestPart这样的参数之后")
    }
}