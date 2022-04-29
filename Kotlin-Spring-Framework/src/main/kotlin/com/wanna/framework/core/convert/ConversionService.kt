package com.wanna.framework.core.convert

/**
 * 这是提供了一个内容转换服务，用来完成类型的转换
 */
interface ConversionService {

    /**
     * 能否将源类型的对象转换为目标类型？
     *
     * @param sourceType 源类型
     * @param targetType 目标类型
     * @return 如果能进行转换，return true；不然return false
     */
    fun canConvert(sourceType: Class<*>, targetType: Class<*>): Boolean

    /**
     * 将source，转换为目标类型(targetType)
     * @param source 源对象(可以为空)
     * @param targetType 要转换的目标类型
     */
    fun <T> convert(source: Any?, targetType: Class<T>): T?
}