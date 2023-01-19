package com.wanna.framework.web.method.annotation

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.web.HandlerMapping
import com.wanna.framework.web.HandlerMapping.Companion.PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE
import com.wanna.framework.web.accept.ContentNegotiationManager
import com.wanna.framework.web.context.request.NativeWebRequest
import com.wanna.framework.web.context.request.ServerWebRequest
import com.wanna.framework.web.http.MediaType
import com.wanna.framework.web.http.converter.HttpMediaTypeNotAcceptableException
import com.wanna.framework.web.http.converter.HttpMessageConverter
import com.wanna.framework.web.http.converter.HttpMessageNotReadableException
import com.wanna.framework.web.http.converter.HttpMessageNotWritableException
import com.wanna.framework.web.http.server.ServerHttpRequest
import com.wanna.framework.web.http.server.ServerHttpResponse
import com.wanna.framework.web.method.support.HandlerMethodReturnValueHandler
import com.wanna.framework.web.server.HttpServerRequest

/**
 * 这是一个抽象的MessageConverter的方法处理器, 它的父类AbstractMessageConverterMethodArgumentResolver支持了使用MessageConverter去完成RequestBody的读取,
 * 在这个类当中, 新增支持使用MessageConverter去完成ResponseBody的写出到HTTP响应体当中的代码的实现, 它同时也实现了HandlerMethodReturnValueHandler, 说明这个类
 * 既是一个方法的参数处理器, 同时也是一个方法的返回值处理器;
 *
 * 它也组合了内容协商管理器(ContentNegotiationManager), 内部组合了多种协商策略, 支持去从request当中去获取客户端想要接收的媒体类型
 *
 * @see AbstractMessageConverterMethodArgumentResolver
 * @see com.wanna.framework.web.http.converter.HttpMessageConverter
 */
abstract class AbstractMessageConverterMethodProcessor : AbstractMessageConverterMethodArgumentResolver(),
    HandlerMethodReturnValueHandler {

    // 内容协商管理器, 负责去获取客户端想要去进行接收的媒体类型
    private var contentNegotiationManager = ContentNegotiationManager()

    /**
     * 使用MessageConverter去进行写出, 将方法的返回值去以合适的方式去进行序列化, 并将序列化的结果写入到HTTP响应报文的ResponseBody当中
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
     * 使用MessageConverter去进行写出, 将方法的返回值去以合适的方式去进行序列化, 并将序列化的结果写入到HTTP响应报文的ResponseBody当中
     * 存在着多种类型的转换(比如转json/xml)的MessageConverter, 需要遍历所有的HttpMessageConverter, 去找到一个合适的处理本次请求的写出的MessageConverter
     *
     * @param value 要进行写出的值?
     * @param returnType 返回值类型封装的MethodParameter
     * @param inputMessage 输入流
     * @param outputMessage 输出流
     */
    @Suppress("UNCHECKED_CAST")
    protected open fun <T> writeWithMessageConverters(
        value: T?, returnType: MethodParameter, inputMessage: ServerHttpRequest, outputMessage: ServerHttpResponse
    ) {
        val parameterType = returnType.getParameterType()

        // 获取ContentType
        val contentType = inputMessage.getHeaders().getContentType()

        val request = inputMessage.getRequest()
        // 使用内容协商管理器, 去获取客户端想要(愿意)接收的所有的MediaType
        val acceptableMediaTypes = getAcceptableMediaTypes(request)
        // 获取服务端所能产出的所有的MediaType
        val producibleMediaTypes = getProducibleMediaTypes(request, parameterType)

        var selectedMediaType: MediaType? = null

        // 如果用户有自定义的ContentType了, 那么直接沿用你给定的ContentType, 不必再去进行推断
        if (contentType != null) {
            selectedMediaType = contentType

            // 如果用户没有自定义的ContentType, 那么需要根据可以接收的和可以产出的类型去进行匹配...
        } else {
            // 遍历客户端可以接收的所有类型的MediaType, 去判断我服务端当前能否产出?
            val mediaTypesToUse = ArrayList<MediaType>()
            for (requestType in acceptableMediaTypes) {
                for (producibleMediaType in producibleMediaTypes) {
                    if (requestType.isCompatibleWith(producibleMediaType)) {
                        mediaTypesToUse += getMostSpecificMediaType(requestType, producibleMediaType)
                    }
                }
            }

            // 根据MediaType的具体程度以及权重去进行MediaType的排序
            MediaType.sortBySpecificityAndQuality(mediaTypesToUse)

            for (mediaType in mediaTypesToUse) {
                if (mediaType.isConcrete) {
                    selectedMediaType = mediaType
                    break
                }
            }
        }


        // 遍历所有的MessageConverter, 挨个去判断它能否完成将当前的返回值类型写出成为指定的MediaType
        // 如果它支持去进行写出的话, 那么就使用该HttpMessageConverter去完成消息的写出
        if (selectedMediaType != null) {
            // remove quality value, such as "application/json;q=0.8"
            selectedMediaType = selectedMediaType.removeQualityValue()
            this.messageConverters.forEach {
                if (it.canWrite(parameterType, selectedMediaType)) {
                    if (value != null) {
                        (it as HttpMessageConverter<T>).write(value, selectedMediaType, outputMessage)
                        return  // return all, no need to continue
                    }
                }
            }
        }

        // handle write failed
        if (value != null) {
            val producibleTypes =
                inputMessage.getRequest().getAttribute(PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE) as Collection<MediaType>?
            if (contentType != null && producibleTypes != null && producibleTypes.isNotEmpty()) {
                throw HttpMessageNotWritableException("找不到合适的MessageConverter去进行将$parameterType 去转换为ContentType=$contentType")
            }
            throw HttpMediaTypeNotAcceptableException(
                "服务端无法产出让客户端可以接受的MediaType, " +
                        "producibleMediaTypes=$producibleMediaTypes, acceptableMediaTypes=$acceptableMediaTypes"
            )
        }
    }

    /**
     * 创建HttpOutputMessage输出流, 处理请求时, 可以将要响应给客户端的数据写入到输出流当中, 后续请求当中, 自动完成写出
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
     * 获取服务端所能产出的全部MediaType类型的列表
     *
     * * 1.尝试从request属性当中去进行获取, 因为有可能通过@RequestMapping等注解的方式去进行过配置, 就会被添加到这个属性当中来
     * * 2.如果request属性当中不存在的话, 那么需要遍历所有的MessageConverter, 看它们能产出什么类型的MediaType
     *
     * @param request request
     * @param valueType JavaBean类型, 交给MessageConverter去进行匹配(如果都不支持处理这样的JavaBean, 那么不需要被统计出来)
     * @return 服务端能够产生的全部MediaType列表
     */
    protected open fun getProducibleMediaTypes(request: HttpServerRequest, valueType: Class<*>): List<MediaType> {

        // 1.尝试从request当中去进行搜索, 如果request属性当中已经存在了的话, 那么直接沿用request
        @Suppress("UNCHECKED_CAST")
        val mediaTypes = request.getAttribute(PRODUCIBLE_MEDIA_TYPES_ATTRIBUTE) as Collection<MediaType>?
        if (mediaTypes != null && mediaTypes.isNotEmpty()) {
            return ArrayList(mediaTypes)
        }

        // 2.遍历所有的MessageConverter去进行搜索它们所支持的MediaType, 完成最终可以产出的MeidaType的统计工作
        val produceTypes = ArrayList<MediaType>()
        this.messageConverters.forEach {
            produceTypes += it.getSupportedMediaTypes(valueType)
        }

        // if empty set to all
        return if (produceTypes.isEmpty()) listOf(MediaType.ALL) else produceTypes
    }

    /**
     * 根据想要接收的媒体类型以及可以产出的媒体类型, 获取更加具体的媒体类型
     *
     * @param acceptType 可以接收的类型
     * @param produceType 可以产出的类型
     * @return 更加具体的媒体类型
     */
    private fun getMostSpecificMediaType(acceptType: MediaType, produceType: MediaType): MediaType {
        val produceToUse = produceType.copyQualityValue(acceptType)
        return if (MediaType.SPECIFICITY_COMPARATOR.compare(acceptType, produceToUse) < 0) acceptType else produceToUse
    }

    /**
     * 设置内容协商管理器, 去替换掉默认的ContentNegotiationManager
     *
     * @param contentNegotiationManager 你想要使用的ContentNegotiationManager
     */
    open fun setContentNegotiationManager(contentNegotiationManager: ContentNegotiationManager) {
        this.contentNegotiationManager = contentNegotiationManager
    }
}