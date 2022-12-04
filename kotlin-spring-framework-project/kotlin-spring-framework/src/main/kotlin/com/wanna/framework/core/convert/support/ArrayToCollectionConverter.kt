package com.wanna.framework.core.convert.support

import com.wanna.framework.core.CollectionFactory
import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.convert.TypeDescriptor
import com.wanna.framework.core.convert.converter.GenericConverter
import com.wanna.framework.util.ClassUtils

/**
 * 将Array转换为Collection的Converter
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/4
 *
 * @param conversionService 因为Array->Collection, 存在有具体的元素类型的转换, 因此需要用到ConversionService
 */
class ArrayToCollectionConverter(private val conversionService: ConversionService) : GenericConverter {

    /**
     * Array->Collection
     */
    override fun getConvertibleTypes(): Set<GenericConverter.ConvertiblePair> =
        setOf(GenericConverter.ConvertiblePair(Array::class.java, Collection::class.java))

    @Suppress("UNCHECKED_CAST")
    override fun <S : Any, T : Any> convert(source: Any?, sourceType: Class<S>, targetType: Class<T>): T? {
        // 只支持去处理source is Array, target is Collection的情况
        if (source is Array<*> && ClassUtils.isAssignFrom(Collection::class.java, targetType)) {
            val collection = CollectionFactory.createCollection<Any?>(targetType, source.size)

            // 我们这里没有办法去处理具体的元素的泛型类型, 暂时pass掉, 直接添加
            source.forEach(collection::add)
            return collection as T?
        }
        return null
    }

    override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
        // 只支持去处理source is Array, target is Collection的情况
        if (source is Array<*> && ClassUtils.isAssignFrom(Collection::class.java, targetType.type)) {
            val collection = CollectionFactory.createCollection<Any?>(targetType.type, source.size)

            // sourceType为Array的ComponentType
            val sourceElementType = sourceType.type.componentType
            // targetType为Collection的elementType
            val targetElementType = targetType.resolvableType.getGenerics()[0].resolve(Any::class.java)

            // 对元素去进行类型转换, 添加到collection当中去
            if (conversionService.canConvert(sourceElementType, targetElementType)) {
                source.forEach { collection.add(conversionService.convert(it, targetElementType)) }
            }
            return collection
        }
        return null
    }
}