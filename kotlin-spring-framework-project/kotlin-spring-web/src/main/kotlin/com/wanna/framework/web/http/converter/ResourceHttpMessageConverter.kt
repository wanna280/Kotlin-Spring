package com.wanna.framework.web.http.converter

import com.wanna.framework.core.io.ByteArrayResource
import com.wanna.framework.core.io.InputStreamResource
import com.wanna.framework.core.io.Resource
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.web.http.HttpInputMessage
import com.wanna.framework.web.http.HttpOutputMessage
import com.wanna.framework.web.http.MediaType
import java.io.FileNotFoundException

/**
 * 对于Spring的[Resource]去进行处理的[HttpMessageConverter]实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/28
 *
 * @param supportsReadStreaming 是否需要支持使用流的方式去进行读取?
 */
open class ResourceHttpMessageConverter(private val supportsReadStreaming: Boolean = true) :
    AbstractHttpMessageConverter<Resource>() {

    init {
        super.setSupportedMediaTypes(MediaType.ALL)
    }

    /**
     * 支持去处理[Resource]类型的Java对象的消息的处理
     *
     * @param clazz Java对象类型
     */
    override fun supports(clazz: Class<*>): Boolean = ClassUtils.isAssignFrom(Resource::class.java, clazz)


    /**
     * 将消息从[HttpInputMessage]当中提取出来转换成为[Resource]去进行返回
     *
     * @param clazz 待转换的Resource类型
     * @param inputMessage Http的RequestBody的输入流信息
     * @return 根据[HttpInputMessage]去进行内容的提取和转换得到的[Resource]
     */
    override fun readInternal(clazz: Class<*>, inputMessage: HttpInputMessage): Resource {
        if (supportsReadStreaming && clazz == InputStreamResource::class.java) {
            return InputStreamResource(inputMessage.getBody())

            // 如果是Resource/ByteArrayResource的话, 直接把内容提取到ByteArrayResource当中去进行返回
        } else if (clazz == Resource::class.java || ClassUtils.isAssignFrom(ByteArrayResource::class.java, clazz)) {
            return ByteArrayResource(inputMessage.getBody().readAllBytes())

            // 不支持去处理别的Resource类型
        } else {
            throw HttpMessageNotReadableException("Unsupported resource class: ${clazz.name}", inputMessage)
        }
    }


    /**
     * 将消息从[Resource]去写入到[HttpOutputMessage]当中去进行返回, 写入到HTTP的RequestBody当中
     *
     * @param t 待写入的Resource
     * @param outputMessage OutputStreamMessage
     */
    override fun writeInternal(t: Resource, mediaType: MediaType?, outputMessage: HttpOutputMessage) {
        writeContent(t, outputMessage)
    }

    /**
     * 将消息从[Resource]去写入到[HttpOutputMessage]当中去进行返回, 写入到HTTP的RequestBody当中
     *
     * @param resource 待写入的Resource
     * @param outputMessage OutputStreamMessage
     */
    protected open fun writeContent(resource: Resource, outputMessage: HttpOutputMessage) {
        try {
            val inputStream = resource.getInputStream()
            try {
                val outputStream = outputMessage.getBody()
                inputStream.transferTo(outputStream)
                outputStream.flush()
            } catch (ex: NullPointerException) {
                // ignore
            } finally {
                try {
                    inputStream.close()
                } catch (ex: Exception) {
                    // ignore
                }
            }

        } catch (ex: FileNotFoundException) {
            // ignore
        }
    }
}