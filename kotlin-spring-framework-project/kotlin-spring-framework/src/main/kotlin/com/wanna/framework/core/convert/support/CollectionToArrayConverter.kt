package com.wanna.framework.core.convert.support

import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.convert.TypeDescriptor
import com.wanna.framework.core.convert.converter.GenericConverter
import com.wanna.framework.core.convert.converter.GenericConverter.*

/**
 * 将Collection转为Array的Converter
 */
open class CollectionToArrayConverter(private val conversionService: ConversionService) : GenericConverter {

    /**
     * Collection->Array
     */
    override fun getConvertibleTypes() = setOf(ConvertiblePair(Collection::class.java, Array::class.java))

    @Suppress("UNCHECKED_CAST")
    override fun <S : Any, T : Any> convert(source: Any?, sourceType: Class<S>, targetType: Class<T>): T? {
        if (source is Collection<Any?> && targetType.isArray) {
            val array = java.lang.reflect.Array.newInstance(targetType.componentType, source.size)
            val iterator = source.iterator()
            for (index in 0 until source.size) {
                java.lang.reflect.Array.set(array, index, iterator.next())
            }
            return array as T?
        }
        return null
    }

    override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
        if (source is Collection<Any?> && targetType.type.isArray) {
            // fixed: 针对集合当中的单个元素去进行类型的转换...

            // sourceType is Collection, 我们去进行注解的泛型解析sourceElementType
            val sourceElementType = sourceType.resolvableType.asCollection().getGenerics()[0].resolve(Any::class.java)

            // targetType is Array, 我们使用componentType去作为targetElementType
            val targetElementType = targetType.type.componentType

            // 利用反射去实例化出来一个Array
            val array = java.lang.reflect.Array.newInstance(targetElementType, source.size)
            val iterator = source.iterator()

            // 完成类型的转换, 并使用反射去设置到Array当中去
            if (conversionService.canConvert(sourceElementType, targetElementType)) {
                for (index in 0 until source.size) {
                    val convertedValue = conversionService.convert(iterator.next(), targetElementType)
                    java.lang.reflect.Array.set(array, index, convertedValue)
                }
            }
            return array
        }
        return null
    }

    override fun toString() = getConvertibleTypes().toString()
}