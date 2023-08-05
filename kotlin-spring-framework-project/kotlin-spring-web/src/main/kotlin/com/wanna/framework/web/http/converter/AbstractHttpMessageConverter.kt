package com.wanna.framework.web.http.converter

import com.wanna.common.logging.Logger
import com.wanna.common.logging.LoggerFactory
import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.http.HttpHeaders
import com.wanna.framework.web.http.HttpInputMessage
import com.wanna.framework.web.http.HttpOutputMessage
import com.wanna.framework.web.http.MediaType
import java.nio.charset.Charset

/**
 * 抽象的[HttpMessageConverter]的实现, 为所有的[HttpMessageConverter]去提供模板方法的实现
 *
 * @see HttpMessageConverter
 */
abstract class AbstractHttpMessageConverter<T> : HttpMessageConverter<T> {

    /**
     * Logger
     */
    protected val logger: Logger = LoggerFactory.getLogger(AbstractHttpMessageConverter::class.java)

    /**
     * 当前的MessageConverter支持的MediaType列表
     */
    private var supportedMediaTypes = ArrayList<MediaType>()

    /**
     * 默认的字符集
     */
    @Nullable
    private var defaultCharset: Charset? = null

    /**
     * 设置支持去进行处理的MediaType
     *
     * @param supportedMediaTypes SupportedMediaTypes
     */
    open fun setSupportedMediaTypes(vararg supportedMediaTypes: MediaType) {
        this.supportedMediaTypes = ArrayList(listOf(*supportedMediaTypes))
    }

    /**
     * 获取默认的字符集
     *
     * @return 默认字符集
     */
    @Nullable
    open fun getDefaultCharset(): Charset? = this.defaultCharset

    /**
     * 设置默认的字符集
     *
     * @param defaultCharset 默认字符集
     */
    open fun setDefaultCharset(defaultCharset: Charset) {
        this.defaultCharset = defaultCharset
    }

    /**
     * 获取当前的MessageConverter支持去进行处理的MediaType列表
     *
     * @return 支持去进行处理的MediaType
     */
    override fun getSupportedMediaTypes() = this.supportedMediaTypes

    /**
     * 能否去读这样的MediaType成为clazz类型的数据? 如果它支持去处理这样的类型的数据, 并且支持这种MediaType的写的功能即可认为它可以读
     *
     * @param clazz JavaBean类型
     * @param mediaType MediaType
     */
    override fun canRead(clazz: Class<*>, @Nullable mediaType: MediaType?) = supports(clazz) && canRead(mediaType)

    /**
     * 能否将clazz类型的JavaBean数据去写出成为MediaType类型的响应数据? 如果它支持去处理这样的类型的数据, 并且支持这种MediaType的写的功能即可认为它可以写
     *
     * @param clazz JavaBean类型
     * @param mediaType MediaType
     */
    override fun canWrite(clazz: Class<*>, @Nullable mediaType: MediaType?) = supports(clazz) && canWrite(mediaType)

    protected open fun canRead(@Nullable mediaType: MediaType?): Boolean {
        mediaType ?: return true
        getSupportedMediaTypes().forEach {
            if (it.includes(mediaType)) {
                return true
            }
        }
        return false
    }

    protected open fun canWrite(@Nullable mediaType: MediaType?): Boolean {
        mediaType ?: return true
        getSupportedMediaTypes().forEach {
            if (it.isCompatibleWith(mediaType)) {
                return true
            }
        }
        return false
    }


    override fun read(clazz: Class<T>, inputMessage: HttpInputMessage): T {
        return readInternal(clazz, inputMessage)
    }

    /**
     * 添加默认的一些HttpHeader
     */
    protected open fun addDefaultHeaders(headers: HttpHeaders, t: T, @Nullable contentType: MediaType?) {
        if (contentType != null) {
            var contentTypeToUse = contentType

            // 如果必要的话, 需要添加上默认的charset
            if (contentTypeToUse.charset == null) {
                val defaultCharset = getDefaultCharset()
                if (defaultCharset != null) {
                    contentTypeToUse = MediaType(contentType, defaultCharset)
                }
            }
            headers.setContentType(contentTypeToUse)  // setContentType
        }
    }


    override fun write(t: T, @Nullable mediaType: MediaType?, outputMessage: HttpOutputMessage) {

        // 添加一些默认的Header到Response当中
        addDefaultHeaders(outputMessage.getHeaders(), t, mediaType)

        // writeInternal
        return writeInternal(t, mediaType, outputMessage)
    }

    /**
     * 是否去处理支持[clazz]这样的Java对象类型数据?
     *
     * @param clazz JavaBean类型
     * @return 如果支持去进行这样的Java对象数据的处理, 那么return true; 否则return false
     */
    protected abstract fun supports(clazz: Class<*>): Boolean

    /**
     * 执行真正的read工作, 模板方法, 具体逻辑交给子类去完成实现
     *
     * @param clazz JavaBean类型
     * @param inputMessage request Message数据
     * @return 完成读取工作最终得到的Java对象
     */
    protected abstract fun readInternal(clazz: Class<*>, inputMessage: HttpInputMessage): T

    /**
     * 执行真正的write工作, 模板方法, 具体逻辑交给子类去进行实现
     *
     * @param t JavaBean
     * @param mediaType MediaType
     * @param outputMessage response Message数据
     */
    protected abstract fun writeInternal(t: T, @Nullable mediaType: MediaType?, outputMessage: HttpOutputMessage)
}