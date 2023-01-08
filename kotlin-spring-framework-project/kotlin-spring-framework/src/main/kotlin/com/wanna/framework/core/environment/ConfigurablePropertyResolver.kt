package com.wanna.framework.core.environment

import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.lang.Nullable

/**
 * 这是一个支持进行配置的PropertyResolver
 */
interface ConfigurablePropertyResolver : PropertyResolver {

    /**
     * 设置占位符的前缀
     */
    fun setPlaceholderPrefix(prefix: String)

    /**
     * 获取占位符的后缀
     */
    fun setPlaceholderSuffix(suffix: String)

    /**
     * 设置默认值的分割符
     */
    fun setValueSeparator(@Nullable separator: String?)

    /**
     * 获取类型转换服务
     */
    fun getConversionService(): ConversionService

    /**
     * 这种类型转换服务
     */
    fun setConversionService(conversionService: ConversionService)
}