package com.wanna.framework.core.convert.support

import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.convert.TypeDescriptor
import com.wanna.framework.core.convert.converter.GenericConverter

/**
 * Map->Map的Converter
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/5
 *
 * @param conversionService 因为存在有对于Map内部的元素的类型转换, 因此需要用到ConversionService
 */
open class MapToMapConverter(private val conversionService: ConversionService) : GenericConverter {

    /**
     * Map->Map
     *
     * @return pair of Map->Map
     */
    override fun getConvertibleTypes(): Set<GenericConverter.ConvertiblePair> =
        setOf(GenericConverter.ConvertiblePair(Map::class.java, Map::class.java))

    /**
     * 对于这种没有泛型的Map情况, 我们没法处理, 暂时先返回原来的了
     */
    @Suppress("UNCHECKED_CAST")
    override fun <S : Any, T : Any> convert(source: Any?, sourceType: Class<S>, targetType: Class<T>): T? {
        return source as T?
    }

    /**
     * 这种情况下, 我们可以拿到泛型, 我们可以对元素类型去进行转换
     *
     * @param source source
     * @param sourceType sourceType
     * @param targetType targetType
     */
    override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
        if (source is Map<*, *>) {
            return source
        }
        return null
    }
}