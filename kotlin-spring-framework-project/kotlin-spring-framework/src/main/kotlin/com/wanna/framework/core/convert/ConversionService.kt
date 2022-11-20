package com.wanna.framework.core.convert

/**
 * 这是Spring实现的内容转换服务，它的主要作用是为Spring当中的各个地方的类型的转换提供支持
 *
 * @see com.wanna.framework.core.convert.converter.Converter
 * @see com.wanna.framework.core.convert.converter.GenericConverter
 * @see com.wanna.framework.core.convert.converter.ConverterRegistry
 * @see com.wanna.framework.core.convert.support.DefaultConversionService
 */
interface ConversionService {

    /**
     * 能否将源类型(sourceType)的对象转换为目标类型(targetType)？
     *
     * @param sourceType 源类型
     * @param targetType 目标类型
     * @return 如果能进行转换，return true；不然return false
     */
    fun canConvert(sourceType: Class<*>, targetType: Class<*>): Boolean

    /**
     * 支持使用TypeDescriptor的方式去进行判断，能否将目标类型转换target类型？
     *
     * @param sourceType sourceType(TypeDescriptor，支持泛型的解析)
     * @param targetType targetType(TypeDescriptor，支持泛型的解析)
     * @return 如果能进行转换，return true；不然return false
     */
    fun canConvert(sourceType: TypeDescriptor, targetType: TypeDescriptor): Boolean

    /**
     * 将source对象，转换为目标类型(targetType)
     *
     * @param source 源对象(可以为空)
     * @param targetType 要转换的目标类型
     */
    fun <T : Any> convert(source: Any?, targetType: Class<T>): T?

    /**
     * 将source，转换为目标类型(targetType)，支持去解析targetType的泛型信息
     *
     * @param source 源对象(可以为空)
     * @param targetType 要转换的目标类型(TypeDescriptor，支持泛型的解析)
     * @return 转换之后的结果
     */
    fun convert(source: Any?, targetType: TypeDescriptor): Any?
}