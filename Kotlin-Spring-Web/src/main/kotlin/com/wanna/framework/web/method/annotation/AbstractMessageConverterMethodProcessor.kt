package com.wanna.framework.web.method.annotation

import com.fasterxml.jackson.databind.ObjectMapper
import com.wanna.framework.core.MethodParameter
import com.wanna.framework.web.accept.ContentNegotiationManager
import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.context.request.ServerWebRequest
import com.wanna.framework.web.http.MediaType
import com.wanna.framework.web.http.converter.HttpMessageConverter
import com.wanna.framework.web.http.server.ServerHttpRequest
import com.wanna.framework.web.http.server.ServerHttpResponse
import com.wanna.framework.web.method.support.HandlerMethodReturnValueHandler
import com.wanna.framework.web.server.HttpServerRequest

/**
 * 这是一个抽象的MessageConverter的方法处理器，它的父类AbstractMessageConverterMethodArgumentResolver支持了使用MessageConverter去完成RequestBody的读取，
 * 在这个类当中，新增支持使用MessageConverter去完成ResponseBody的写出到HTTP响应体当中的代码的实现，它同时也实现了HandlerMethodReturnValueHandler，说明这个类
 * 既是一个方法的参数处理器，同时也是一个方法的返回值处理器；
 *
 * 它也组合了内容协商管理器(ContentNegotiationManager)，内部组合了多种协商策略，支持去从request当中去获取客户端想要接收的媒体类型
 *
 * @see AbstractMessageConverterMethodArgumentResolver
 * @see com.wanna.framework.web.http.converter.HttpMessageConverter
 */
abstract class AbstractMessageConverterMethodProcessor : AbstractMessageConverterMethodArgumentResolver(),
    HandlerMethodReturnValueHandler {

    // 内容协商管理器，负责去获取客户端想要去进行接收的媒体类型
    private var contentNegotiationManager = ContentNegotiationManager()

    /**
     * 使用MessageConverter去进行写出，将方法的返回值去以合适的方式去进行序列化，并将序列化的结果写入到HTTP响应报文的ResponseBody当中
     *
     * @param value 要进行写出的值
     * @param returnType 方法返回类型封装的MethodParameter
     * @param nativeWebRequest NativeWebRequest(request and response)
     */
    protected open fun <T> writeWithMessageConverters(
        value: T?, returnType: MethodParameter, nativeWebRequest: NativeWebRequest
    ) {
        val inputMessage = createInputMessage(nativeWebRequest)
        val outputMessage = createOutputMessage(nativeWebRequest)

        writeWithMessageConverters(value, returnType, inputMessage, outputMessage)
    }

    /**
     * 使用MessageConverter去进行写出，将方法的返回值去以合适的方式去进行序列化，并将序列化的结果写入到HTTP响应报文的ResponseBody当中
     * 存在着多种类型的转换(比如转json/xml)的MessageConverter，需要遍历所有的HttpMessageConverter，去找到一个合适的处理本次请求的写出的MessageConverter
     *
     * @param value 要进行写出的值？
     * @param returnType 返回值类型封装的MethodParameter
     * @param inputMessage 输入流
     * @param outputMessage 输出流
     */
    @Suppress("UNCHECKED_CAST")
    protected open fun <T> writeWithMessageConverters(
        value: T?, returnType: MethodParameter, inputMessage: ServerHttpRequest, outputMessage: ServerHttpResponse
    ) {
        val parameterType = returnType.getParameterType()

        val request = inputMessage.getRequest()
        // 使用内容协商管理器，去获取客户端想要(愿意)接收的所有的MediaType
        val acceptableMediaTypes = getAcceptableMediaTypes(request)
        // 获取服务端所能产出的所有的MediaType
        val producibleMediaTypes = getProducibleMediaTypes(request, parameterType)

        // 遍历客户端可以接收的所有类型的MediaType，去判断我服务端能否产出？
        val mediaTypesToUse = ArrayList<MediaType>()
        for (requestType in acceptableMediaTypes) {
            for (producibleMediaType in producibleMediaTypes) {
                if (requestType.isCompatibleWith(producibleMediaType)) {
                    mediaTypesToUse += getMostSpecificMediaType(requestType, producibleMediaType)
                }
            }
        }

        MediaType.sortBySpecificityAndQuality(mediaTypesToUse)

        var mediaTypeToUse: MediaType? = null
        for (mediaType in mediaTypesToUse) {
            if (mediaType.isConcrete) {
                mediaTypeToUse = mediaType
                break
            }
        }
        if (mediaTypeToUse != null) {
            this.messageConverters.forEach {
                if (it.canWrite(parameterType, mediaTypeToUse)) {
                    if (value != null) {
                        (it as HttpMessageConverter<T>).write(value, mediaTypeToUse, outputMessage)
                    }
                }
            }
        }
    }

    /**
     * 创建HttpOutputMessage输出流，处理请求时，可以将要响应给客户端的数据写入到输出流当中，后续请求当中，自动完成写出
     *
     * @param nativeWebRequest NativeWebRequest(request and response)
     * @return 输出流
     */
    protected open fun createOutputMessage(nativeWebRequest: NativeWebRequest): ServerHttpResponse {
        return ServerHttpResponse(nativeWebRequest)
    }

    /**
     * 使用内容协商管理器从request当中去解析到获取到客户端想要接收的MediaType列表
     *
     * @param request
     * @return 获取客户段可以接收的MediaType列表
     */
    private fun getAcceptableMediaTypes(request: HttpServerRequest): List<MediaType> {
        return this.contentNegotiationManager.resolveMediaTypes(ServerWebRequest(request))
    }

    /**
     * 获取服务端所能产出的全部MediaType类型
     *
     * @param request
     * @return 服务端能够产生的全部MediaType
     */
    private fun getProducibleMediaTypes(request: HttpServerRequest, valueType: Class<*>): List<MediaType> {
        val produceTypes = ArrayList<MediaType>()
        this.messageConverters.forEach {
            produceTypes += it.getSupportedMediaTypes(valueType)
        }
        return produceTypes
    }

    /**
     * 获取更加具体的媒体类型
     *
     * @param acceptType 可以接收的类型
     * @param produceType 可以产出的类型
     * @return 更加具体的媒体类型
     */
    private fun getMostSpecificMediaType(acceptType: MediaType, produceType: MediaType): MediaType {
        val produceToUse = produceType.copyQualityValue(acceptType)
        return if (MediaType.SPECIFICITY_COMPARATOR.compare(acceptType, produceToUse) < 0) acceptType else produceToUse
    }
}