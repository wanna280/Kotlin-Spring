package com.wanna.framework.web.method.annotation

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.web.accept.ContentNegotiationManager
import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.http.converter.HttpMessageConverter
import com.wanna.framework.web.method.support.ModelAndViewContainer
import org.springframework.core.annotation.AnnotatedElementUtils

/**
 * 这是一个处理@RequestBody、@ResponseBody注解的方法的处理器，它既是一个参数解析器，同时也是一个返回值处理器；
 * 它的父类当中，提供了使用MessageConverter完成RequestBody的读取，也提供了使用MessageConverter完成ResponseBody的写入的代码实现；
 * 在这个类当中，只需要使用配合父类MessageConverter相关的方法，即可去完成@RequestBody和@ResponseBody注解的处理
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
     * 它支持处理什么样的参数？方法参数上标注了@RequestBody的参数即可支持处理
     *
     * @param parameter 方法参数
     * @return 是否支持处理这样的参数？
     */
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.getAnnotation(RequestBody::class.java) != null
    }

    /**
     * 它支持处理什么样的返回值？只要目标类的HandlerMethod上标注了@ResponseBody的返回值，或者类上标注了@ResponseBody即可支持处理
     *
     * @param parameter 返回值类型
     * @return 是否支持处理这样的返回值类型？
     */
    override fun supportsReturnType(parameter: MethodParameter): Boolean {
        return parameter.getAnnotation(ResponseBody::class.java) != null || AnnotatedElementUtils.isAnnotated(
            parameter.getContainingClass()!!, ResponseBody::class.java
        )
    }

    /**
     * 处理返回值，只需要使用HttpMessageConverter，去将返回值的JavaBean去进行写出到response的ResponseBody当中即可
     *
     * @param returnValue 方法的返回值，可能为null
     * @param webRequest NativeWebRequest(request and response)
     * @param returnType 方法的返回值类型
     */
    override fun handleReturnValue(
        returnValue: Any?,
        webRequest: NativeWebRequest,
        returnType: MethodParameter,
        mavContainer: ModelAndViewContainer
    ) {
        writeWithMessageConverters(returnValue, returnType, webRequest)
    }

    /**
     * 解析方法参数，只需要使用HttpMessageConverter，去将request当中的RequestBody转换为JavaBean即可
     *
     * @param parameter 方法参数
     * @param webRequest NativeWebRequest(request and response)
     * @return HttpMessageConverter转换出来的RequestBody
     */
    override fun resolveArgument(
        parameter: MethodParameter, webRequest: NativeWebRequest, mavContainer: ModelAndViewContainer?
    ): Any? {
        return readWithMessageConverters<Any>(webRequest, parameter, parameter.getParameterType())
    }
}