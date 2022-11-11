package com.wanna.framework.core.convert.support

import com.wanna.framework.core.CollectionFactory
import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.convert.TypeDescriptor
import com.wanna.framework.core.convert.converter.GenericConverter
import com.wanna.framework.core.convert.converter.GenericConverter.ConvertiblePair
import com.wanna.framework.util.StringUtils

/**
 * String转Collection的的Converter
 *
 * @param conversionService 用于Collection内部类型的元素的转换的ConversionService
 */
open class StringToCollectionConverter(private val conversionService: ConversionService) : GenericConverter {
    override fun getConvertibleTypes() = setOf(ConvertiblePair(String::class.java, Collection::class.java))

    @Suppress("UNCHECKED_CAST")
    override fun <S, T> convert(source: Any?, sourceType: Class<S>, targetType: Class<T>): T? {
        val collection = CollectionFactory.createCollection<Any?>(targetType, 16)
        return StringUtils.commaDelimitedListToStringArray(source as String).toCollection(collection) as T
    }

    override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
        source ?: return null
        val resolvableType = targetType.resolvableType.asCollection()
        val generic = resolvableType.getGenerics()[0]

        val collection = CollectionFactory.createCollection<Any?>(targetType.type, 16)

        val sourceList = StringUtils.commaDelimitedListToStringArray(source as String)
        val resolvedGeneric = generic.resolve()

        // bugfix: 如果解析不到泛型，那么直接把原始的字符串数组去转为Collection返回
        if (resolvedGeneric == null) {
            sourceList.toCollection(collection)

            // 如果可以解析到泛型的话，那么才使用ConversionService去进行转换
        } else {
            sourceList.map { conversionService.convert(it, resolvedGeneric) }.toCollection(collection)
        }
        return collection
    }

    override fun toString() = getConvertibleTypes().toString()
}