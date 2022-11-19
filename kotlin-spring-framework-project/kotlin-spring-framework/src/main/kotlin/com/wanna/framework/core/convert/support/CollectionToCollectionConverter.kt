package com.wanna.framework.core.convert.support

import com.wanna.framework.core.CollectionFactory
import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.convert.TypeDescriptor
import com.wanna.framework.core.convert.converter.GenericConverter

/**
 * 将Collection转为Collection的Converter
 */
class CollectionToCollectionConverter(private val conversionService: ConversionService) : GenericConverter {

    override fun getConvertibleTypes(): Set<GenericConverter.ConvertiblePair>? {
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
//        val sourceElementType = sourceType.resolvableType.asCollection().getGenerics()[0]
//        val targetElementType = targetType.resolvableType.asCollection().getGenerics()[0]
//        if (!conversionService.canConvert(sourceElementType.resolve()!!, targetElementType.resolve()!!)) {
//            return false
//        }
        if (source == null || source !is Collection<*>) {
            return null
        }
        val result = CollectionFactory.createCollection<Any?>(targetType.type, source.size)
        source.forEach(result::add)
        return result
    }

    override fun toString() = getConvertibleTypes().toString()

}