package com.wanna.boot.autoconfigure.http

import com.wanna.framework.web.config.annotation.WebMvcConfigurationSupport
import com.wanna.framework.web.http.converter.HttpMessageConverter

/**
 * 维护了[HttpMessageConverter]列表
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/27
 *
 * @param addDefaultConverters 是否需要添加默认的[HttpMessageConverter]?
 * @param additionalConverters 额外要使用的[HttpMessageConverter]
 */
open class HttpMessageConverters(
    addDefaultConverters: Boolean,
    additionalConverters: Collection<HttpMessageConverter<*>>
) : Iterable<HttpMessageConverter<*>> {

    /**
     * 提供一个基于变长的[HttpMessageConverter]列表的构造器
     *
     * @param additionalConverters 额外的HttpMessageConverters
     */
    constructor(vararg additionalConverters: HttpMessageConverter<*>) : this(true, additionalConverters.toList())

    private val messageConverters = ArrayList<HttpMessageConverter<*>>()

    init {
        if (addDefaultConverters) {
            val defaultConverters = object : WebMvcConfigurationSupport() {
                fun getDefaultConverters(): List<HttpMessageConverter<*>> = super.getMessageConverters()
            }.getDefaultConverters()

            this.messageConverters.addAll(defaultConverters)
        }
        this.messageConverters.addAll(additionalConverters)
    }


    open fun getConverters(): List<HttpMessageConverter<*>> = this.messageConverters

    override fun iterator(): Iterator<HttpMessageConverter<*>> = this.messageConverters.iterator()
}