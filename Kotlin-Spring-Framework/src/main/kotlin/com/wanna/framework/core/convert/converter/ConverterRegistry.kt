package com.wanna.framework.core.convert.converter

/**
 * 这是一个Converter的注册中心，可以通过它去管理TypeConverter的增删改查
 */
interface ConverterRegistry {

    /**
     * 添加Converter
     */
    fun addConverter(converter: Converter<*, *>)

    /**
     * 指定source和target的类型，去添加Converter
     */
    fun <S, T> addConverter(sourceType: Class<S>, targetType: Class<T>, converter: Converter<S, T>)

    fun addConverter(converter: GenericConverter)
}