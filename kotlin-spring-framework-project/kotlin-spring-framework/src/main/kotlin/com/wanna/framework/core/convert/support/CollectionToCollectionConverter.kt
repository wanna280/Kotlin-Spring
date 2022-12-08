package com.wanna.framework.core.convert.support

import com.wanna.framework.core.CollectionFactory
import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.convert.TypeDescriptor
import com.wanna.framework.core.convert.converter.GenericConverter

/**
 * 将Collection转为Collection的Converter
 *
 * @param conversionService ConversionService, 因为对于Collection来说, 还需要对内部的元素去进行类型转换, 因此还需要用到ConversionService
 */
class CollectionToCollectionConverter(private val conversionService: ConversionService) : GenericConverter {

    /**
     * Collection->Collection
     */
    override fun getConvertibleTypes(): Set<GenericConverter.ConvertiblePair> {
        return setOf(GenericConverter.ConvertiblePair(Collection::class.java, Collection::class.java))
    }

    @Suppress("UNCHECKED_CAST")
    override fun <S : Any, T : Any> convert(source: Any?, sourceType: Class<S>, targetType: Class<T>): T? {
        if (source == null || source !is Collection<*>) {
            return null
        }
        val result = CollectionFactory.createCollection<Any?>(targetType, source.size)
        source.forEach(result::add)
        return result as T?
    }

    override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
        if (source == null || source !is Collection<*>) {
            return null
        }
        val result = CollectionFactory.createCollection<Any?>(targetType.type, source.size)

        // fixed: 针对集合当中的单个元素去进行类型的转换...
        val sourceElementType = sourceType.resolvableType.asCollection().getGenerics()[0].resolve(Any::class.java)
        val targetElementType = targetType.resolvableType.asCollection().getGenerics()[0].resolve(Any::class.java)

        // 对Collection当中的每个元素尝试去进行类型的转换
        if (conversionService.canConvert(sourceElementType, targetElementType)) {
            source.forEach {
                result.add(conversionService.convert(it, targetElementType))
            }
        }

        return result
    }

    override fun toString() = getConvertibleTypes().toString()

}