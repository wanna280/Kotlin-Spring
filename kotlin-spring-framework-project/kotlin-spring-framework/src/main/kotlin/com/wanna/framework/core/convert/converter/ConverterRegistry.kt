package com.wanna.framework.core.convert.converter

/**
 * 这是一个Converter的注册中心，可以通过它去管理Converter，提供相关的增删改查操作
 *
 * @see Converter
 * @see GenericConverter
 */
interface ConverterRegistry {

    /**
     * 添加Converter，如果能够解析出来Converter的泛型类型，那么自动去进行解析；
     * 如果解析不出来Converter的泛型类型，那么抛出异常(如果是Lambda表达式的方式，很可能推断不出来类型)
     *
     * @param converter 你想要添加的Converter
     * @throws IllegalStateException 如果无法解析到Converter的泛型类型
     */
    fun addConverter(converter: Converter<*, *>)

    /**
     * 指定source和target的类型，去进行添加Converter
     *
     * @param sourceType sourceType
     * @param targetType targetType
     * @param converter 想要添加的Converter
     */
    fun <S : Any, T : Any> addConverter(sourceType: Class<S>, targetType: Class<T>, converter: Converter<S, T>)

    /**
     * 添加一个GenericConverter(在GenericConverter当中需要给出支持的类型，就不必指出泛型类型了)
     *
     * @param converter converter
     */
    fun addConverter(converter: GenericConverter)

    /**
     * 移除一个处理sourceType-->targetType的Converter
     *
     * @param sourceType sourceType
     * @param targetType targetType
     */
    fun removeConvertible(sourceType: Class<*>, targetType: Class<*>)
}