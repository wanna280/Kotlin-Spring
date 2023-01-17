package com.wanna.framework.web.method.annotation

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.validation.BindingResult
import com.wanna.framework.validation.BindingResult.Companion.MODEL_KEY_PREFIX
import com.wanna.framework.web.accept.ContentNegotiationManager
import com.wanna.framework.web.bind.MethodArgumentNotValidException
import com.wanna.framework.web.bind.annotation.RequestBody
import com.wanna.framework.web.bind.annotation.ResponseBody
import com.wanna.framework.web.bind.support.WebDataBinderFactory
import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.http.converter.HttpMessageConverter
import com.wanna.framework.web.method.support.ModelAndViewContainer

/**
 * 这是一个处理@RequestBody、@ResponseBody注解的方法的处理器, 它既是一个参数解析器, 同时也是一个返回值处理器; 
 * 它的父类当中, 提供了使用MessageConverter完成RequestBody的读取, 也提供了使用MessageConverter完成ResponseBody的写入的代码实现; 
 * 在这个类当中, 只需要使用配合父类MessageConverter相关的方法, 即可去完成@RequestBody和@ResponseBody注解的处理
 *
 * @see ResponseBody
 * @see RequestBody
 */
open class RequestResponseBodyMethodProcessor(
    messageConverters: List<HttpMessageConverter<*>>, contentNegotiationManager: ContentNegotiationManager
) : AbstractMessageConverterMethodProcessor() {

    init {
        this.messageConverters += messageConverters
        this.setContentNegotiationManager(contentNegotiationManager)
    }

    /**
     * 它支持处理什么样的参数? 方法参数上标注了@RequestBody的参数即可支持处理
     *
     * @param parameter 方法参数
     * @return 是否支持处理这样的参数?
     */
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.getAnnotation(RequestBody::class.java) != null
    }

    /**
     * 它支持处理什么样的返回值? 只要目标类的HandlerMethod上标注了@ResponseBody的返回值, 或者类上标注了@ResponseBody即可支持处理
     *
     * @param parameter 返回值类型
     * @return 是否支持处理这样的返回值类型?
     */
    override fun supportsReturnType(parameter: MethodParameter): Boolean {
        return parameter.hasMethodAnnotation(ResponseBody::class.java)
                || AnnotatedElementUtils.hasAnnotation(parameter.getContainingClass(), ResponseBody::class.java)
    }

    /**
     * 处理返回值, 只需要使用HttpMessageConverter, 去将返回值的JavaBean去进行写出到response的ResponseBody当中即可
     *
     * @param returnValue 方法的返回值, 可能为null
     * @param webRequest NativeWebRequest(request and response)
     * @param mavContainer ModelAndViewContainer
     * @param returnType 方法的返回值类型
     */
    override fun handleReturnValue(
        returnValue: Any?,
        webRequest: NativeWebRequest,
        returnType: MethodParameter,
        mavContainer: ModelAndViewContainer
    ) {
        // 对于ResponseBody的情况下, 直接设置请求已经被处理了, 因此之后就不必去进行渲染视图了...
        mavContainer.requestHandled = true
        writeWithMessageConverters(returnValue, returnType, webRequest)
    }

    /**
     * 解析方法参数, 只需要使用HttpMessageConverter, 去将request当中的RequestBody转换为JavaBean即可
     *
     * @param parameter 方法参数
     * @param webRequest NativeWebRequest(request and response)
     * @return HttpMessageConverter转换出来的RequestBody
     */
    override fun resolveArgument(
        parameter: MethodParameter,
        webRequest: NativeWebRequest,
        mavContainer: ModelAndViewContainer?,
        binderFactory: WebDataBinderFactory?
    ): Any? {

        // 1.使用MessageConverter去对HTTP的请求体数据去进行读取, 并转换成为JavaBean
        val arg = readWithMessageConverters<Any>(webRequest, parameter, parameter.getParameterType())
        val name = parameter.getParameterName() ?: throw IllegalStateException("无法获取该方法参数上的参数名")
        if (binderFactory != null) {
            // 创建一个WebDataBinder
            val dataBinder = binderFactory.createBinder(webRequest, arg, name)
            if (arg != null) {
                // 检验参数是否合法?
                validateIfApplicable(dataBinder, parameter)

                // 如果DataBinder的BindingResult当中存在有Errors, 并且当前参数的下一个参数并不是BindingResult的话
                // 需要丢出MethodArgumentNotValidException去告诉用户检验方法参数时发生了异常, 需要去进行自定义的处理
                if (dataBinder.getBindingResult().hasErrors() && isBindExceptionRequired(dataBinder, parameter)) {
                    throw MethodArgumentNotValidException(dataBinder.getBindingResult(), parameter)
                }
            }
            // 将BindingResult添加到ModelAndViewContainer的属性当中
            mavContainer?.addAttribute(MODEL_KEY_PREFIX + name, dataBinder.getBindingResult())
        }
        return arg
    }
}