package com.wanna.framework.web.http.converter

import com.wanna.framework.lang.Nullable
import com.wanna.framework.web.http.HttpInputMessage
import com.wanna.framework.web.http.HttpOutputMessage
import com.wanna.framework.web.http.MediaType

/**
 * 这是一个HTTP的MessageConverter, 负责完成HTTP请求当中的RequestBody和ResponseBody的处理工作;
 * (1)通过canRead, 判断能否将指定的mediaType的HTTP RequestBody读取为指定类型的JavaBean; 如果支持的话, 需要实现read方法去进行转换
 * (2)通过canWrite, 判断能否将指定类型的JavaBean输出为指定的媒体类型的HTTP ResponseBody; 如果支持的话, 需要实现write去实现转换
 *
 * @param T 要进行转换的类型
 */
interface HttpMessageConverter<T> {

    /**
     * 获取当前MessageConverter所支持的所有媒体类型？
     *
     * @return 所支持的所有媒体类型
     */
    fun getSupportedMediaTypes(): List<MediaType>

    /**
     * 在对指定类型(clazz)可读/可写的情况下, 获取当前的MessageConverter所支持的所有媒体类型
     *
     * @param clazz 目标类型
     * @return 在对指定类型(clazz)可读/可写的情况下, 该MessageConverter所支持的所有媒体类型列表; 如果不可读也不可写, return emptyList
     */
    fun getSupportedMediaTypes(clazz: Class<*>): List<MediaType> {
        return if (canRead(clazz, null) || canWrite(clazz, null)) getSupportedMediaTypes() else emptyList()
    }

    /**
     * 是否支持读取这样的MediaType
     *
     * @param clazz 这个HttpMessageConverter能否支持将mediaType转换成为这个类型
     * @param mediaType 媒体类型
     * @return 能否转换？
     */
    fun canRead(clazz: Class<*>, @Nullable mediaType: MediaType?): Boolean

    /**
     * 是否支持写这样的MediaType？
     *
     * @param clazz 这个HttpConverter能否支持将clazz类型转换成为这个mediaType类型
     * @param mediaType 媒体类型
     * @return 能否支持转换
     */
    fun canWrite(clazz: Class<*>, @Nullable mediaType: MediaType?): Boolean

    /**
     * 将RequestBody从HTTP的输入流读取为clazz类型的JavaBean
     *
     * @param clazz 要转换成为的类型
     * @param inputMessage 输入流
     * @return 将inputMessage转换为T类型
     */
    fun read(clazz: Class<T>, inputMessage: HttpInputMessage): T

    /**
     * 将JavaBean转换为MediaType, 通过输出流的方式写出给HTTP响应体
     * @param t javaBean
     * @param mediaType 媒体类型
     * @param outputMessage 输出流
     */
    fun write(t: T, @Nullable mediaType: MediaType?, outputMessage: HttpOutputMessage)
}