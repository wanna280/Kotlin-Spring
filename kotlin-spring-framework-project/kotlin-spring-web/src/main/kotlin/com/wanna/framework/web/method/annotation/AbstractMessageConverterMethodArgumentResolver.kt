package com.wanna.framework.web.method.annotation

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.core.annotation.MergedAnnotation
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.validation.DataBinder
import com.wanna.framework.validation.Errors
import com.wanna.framework.validation.annotation.Validated
import com.wanna.framework.web.bind.annotation.RequestBody
import com.wanna.framework.web.bind.annotation.RequestMethod
import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.http.HttpHeaders
import com.wanna.framework.web.http.HttpInputMessage
import com.wanna.framework.web.http.MediaType
import com.wanna.framework.web.http.converter.HttpMessageConverter
import com.wanna.framework.web.http.converter.HttpMessageNotReadableException
import com.wanna.framework.web.http.server.ServerHttpRequest
import com.wanna.framework.web.method.support.HandlerMethodArgumentResolver
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.PushbackInputStream
import java.lang.reflect.Type

/**
 * 这是一个抽象的MessageConverter的方法参数解析器，它支持使用MessageConverter去完成方法参数的解析；
 * 它为使用MessageConverter去将HTTP请求体转换为JavaBean提供了标准的模板代码实现，要想使用MessageConverter去读取Http请求体；
 * 只需要继承当前类即可拥有相关的模板方法去进行实现
 *
 * @see HandlerMethodArgumentResolver
 * @see HttpMessageConverter
 */
abstract class AbstractMessageConverterMethodArgumentResolver : HandlerMethodArgumentResolver {

    companion object {
        // 它支持的请求方式，包括Post/Put/Patch三种方式
        val SUPPORTED_METHODS = setOf(RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH)

        // 空参数标识
        val NO_VALUE = Any()
    }

    // Http消息转换器(MessageConverter)列表
    protected val messageConverters = ArrayList<HttpMessageConverter<*>>()

    /**
     * 使用MessageConverter列表，去读取合适的HTTP请求体当中的数据成为JavaBean
     *
     * @param nativeWebRequest NativeWebRequest(request and response)
     * @param parameter 要读取的方法参数
     * @param type 要读取成为的JavaBean的类型
     * @return 使用HttpMessageConverter转换之后的JavaBean
     */
    protected open fun <T> readWithMessageConverters(
        nativeWebRequest: NativeWebRequest, parameter: MethodParameter, type: Type
    ): Any? {
        val inputMessage: HttpInputMessage = createInputMessage(nativeWebRequest)

        // 使用HttpMessageConverter去进行RequestBody的读取
        val arg = readWithMessageConverters<T>(inputMessage, parameter, type)

        // 如果required=true，但是当前解析的结果为空，丢异常
        if (arg == null && checkRequired(parameter)) {
            throw HttpMessageNotReadableException(
                "读取HttpMessage失败, @RequestBody的required=true，但是HTTP请求当中没有RequestBody",
                inputMessage
            )
        }
        return arg
    }

    /**
     * 检查该参数当中的@RequestBody是否是必须的？
     *
     * @param parameter 方法参数
     * @return @RequestBody注解上的required属性(true/false)
     */
    private fun checkRequired(parameter: MethodParameter): Boolean {
        val requestBody = parameter.getAnnotation(RequestBody::class.java)
        return requestBody != null && requestBody.required
    }


    /**
     * 使用MessageConverter列表，去读取合适的HTTP请求体当中的数据成为JavaBean
     *
     * @param inputMessage 输入流
     * @param parameter 方法参数
     * @param type 要进行读取的JavaBean类型
     * @return 使用HttpMessageConverter转换之后的类型
     */
    @Suppress("UNCHECKED_CAST")
    protected open fun <T> readWithMessageConverters(
        inputMessage: HttpInputMessage, parameter: MethodParameter, type: Type
    ): Any? {
        val contentType: String = inputMessage.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE) ?: ""
        val mediaType = try {
            MediaType.parseMediaType(contentType)
        } catch (ex: Exception) {
            null
        }
        var targetClass: Class<T>? = if (type is Class<*>) type as Class<T> else null
        if (targetClass == null) {
            targetClass = parameter.getParameterType() as Class<T>
        }
        var body: Any? = null

        // 构建一个用来去支持进行RequestBody是否为空的检测的HttpInputMessage
        val message: EmptyBodyCheckingHttpInputMessage
        try {
            message = EmptyBodyCheckingHttpInputMessage(inputMessage)
            this.messageConverters.forEach {
                if (message.hasBody()) {
                    if (it.canRead(targetClass, mediaType)) {
                        body = (it as HttpMessageConverter<T>).read(targetClass, message)
                    }
                }
            }
        } catch (ex: IOException) {
            throw ex
        }
        return body
    }

    /**
     * 创建HttpInputMessage，去将Request当中的RequestBody以流的方式去进行读取
     *
     * @param nativeWebRequest NativeWebRequest(request and response)
     * @return 输入流(HttpInputMessage)
     */
    protected open fun createInputMessage(nativeWebRequest: NativeWebRequest): ServerHttpRequest {
        return ServerHttpRequest(nativeWebRequest)
    }

    /**
     * 对目标方法参数去进行参数检验，检查Spring的`@Validated`注解，以及JSR303当中的`Valid`注解等情况
     *
     * @param webDataBinder WebDataBinder
     * @param parameter 目标方法参数
     */
    protected open fun validateIfApplicable(webDataBinder: DataBinder, parameter: MethodParameter) {
        parameter.getParameterAnnotations().forEach {
            val validated =
                AnnotatedElementUtils.getMergedAnnotationAttributes(it.annotationClass.java, Validated::class.java)
            // 如果它标注了@Validated注解，或者是它是一个以Valid开头的注解，例如JSR303当中的@Valid注解
            if (validated != null || it.annotationClass.java.simpleName.startsWith("Valid")) {

                // 从@Validated注解当中找到value属性去作为validationHints(ValidationGroups)
                val hints = validated?.get(MergedAnnotation.VALUE) ?: emptyArray<String>()

                @Suppress("UNCHECKED_CAST")
                val validationHints = (if (hints is Array<*>) hints else arrayOf(hints)) as Array<Any>
                webDataBinder.validate(validationHints)  // Validate by WebDataBinder
                return  // return all
            }
        }
    }

    /**
     * 是否需要去丢出一个绑定参数异常？
     *
     * * 1.如果下一个参数就是Errors(BindingResult)，那么就说明不必丢出异常
     * * 2.如果下一个参数不是Errors(BindingResult)，那么就说明需要丢出异常
     *
     * @param webDataBinder WebDataBinder
     * @param parameter 方法参数
     */
    protected open fun isBindExceptionRequired(webDataBinder: DataBinder, parameter: MethodParameter): Boolean {
        val parameterIndex = parameter.getParameterIndex()
        val parameterTypes = parameter.getExecutable().parameterTypes

        // 检查下一个参数的类型是否是Errors
        val hasBindingResult = parameterIndex + 1 <= parameterTypes.size &&
                ClassUtils.isAssignFrom(Errors::class.java, parameterTypes[parameterIndex + 1])
        return !hasBindingResult
    }

    /**
     * 这是一个支持去进行空的RequestBody的检测的HttpInputMessage，提供了用来判断InputStream当中是否有消息的判断方式
     */
    private class EmptyBodyCheckingHttpInputMessage(inputMessage: HttpInputMessage) : HttpInputMessage {

        companion object {
            // 一个空的InputStream，使用ByteArrayInputStream去进行构造
            private val EMPTY_INPUT_STREAM: InputStream = ByteArrayInputStream(ByteArray(0))
        }

        private var headers = inputMessage.getHeaders()

        private var body: InputStream? = null

        init {
            val inputStream = inputMessage.getBody()

            // 如果该InputStream支持去进行mark
            if (inputStream.markSupported()) {
                inputStream.mark(1)
                this.body = if (inputStream.read() != -1) inputStream else null
                inputStream.reset()

                // 如果该inputStream不支持mark，那么需要创建一个支持回退的InputStream去进行包装
            } else {
                val pushBackInputStream = PushbackInputStream(inputStream)
                val read = pushBackInputStream.read()
                if (read == -1) {
                    this.body = null
                } else {
                    this.body = pushBackInputStream
                    pushBackInputStream.unread(read)
                }
            }
        }

        fun hasBody(): Boolean = this.body != null

        override fun getBody() = body ?: EMPTY_INPUT_STREAM

        override fun getHeaders() = this.headers
    }
}