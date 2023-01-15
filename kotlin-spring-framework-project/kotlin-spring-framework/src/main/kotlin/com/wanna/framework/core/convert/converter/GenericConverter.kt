package com.wanna.framework.core.convert.converter

import com.wanna.framework.core.convert.TypeDescriptor
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils

/**
 * 这是一个支持泛型的解析的Converter
 *
 * @see Converter
 * @see com.wanna.framework.core.convert.ConversionService
 */
interface GenericConverter {

    /**
     * 获取当前的Converter可以去支持去进行转换的类型的映射列表
     *
     * @return 类型转换的映射列表(可以为null)
     */
    @Nullable
    fun getConvertibleTypes(): Set<ConvertiblePair>?

    /**
     * 将source对象从sourceType转换到targetType;
     * 对于类型, 直接使用Class的方式去进行给出, 因此支持的功能比较少, 因此推荐使用带泛型解析的convert方法
     *
     * @param source 要进行转换的对象
     * @param sourceType 源类型
     * @param targetType 目标类型
     * @return 类型转换完成之后的对象
     */
    @Nullable
    fun <S : Any, T : Any> convert(@Nullable source: Any?, sourceType: Class<S>, targetType: Class<T>): T?

    /**
     * 将source对象从sourceType转换到targetType;
     * 对于类型的给出, 采用TypeDescriptor的方式去进行给出, 可以去解析泛型的类型(最常见的就是集合的泛型),
     * 推荐使用这个方法, 因为TypeDescriptor它支持使用泛型的方式去进行转换, 可以去匹配泛型的类型
     *
     * @param source 要进行转换的对象
     * @param sourceType 源类型
     * @param targetType 目标类型
     * @return 类型转换完成之后的对象
     */
    @Nullable
    fun convert(@Nullable source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any?

    /**
     * 这是一个可以转换的类型的Pair对, 维护了sourceType和targetType;
     *
     * ## Note:
     *
     * 需要去重写equals方法和hashCode方法, 保证该类型可以作为Key去参与hash计算, 并获取到对应的value
     *
     * @param sourceType sourceType
     * @param targetType targetType
     */
    class ConvertiblePair(val sourceType: Class<*>, val targetType: Class<*>) {
        override fun hashCode() = 31 * sourceType.hashCode() + targetType.hashCode()
        override fun toString() =
            "${ClassUtils.getQualifiedName(sourceType)} --> ${ClassUtils.getQualifiedName(targetType)}"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as ConvertiblePair
            return sourceType == other.sourceType && targetType == other.targetType
        }
    }
}