package com.wanna.framework.core.convert.support

import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.convert.converter.GenericConverter
import com.wanna.framework.core.util.ClassUtils
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashSet

/**
 * 将Collection转为Collection的Converter
 */
class CollectionToCollectionConverter(private val conversionService: ConversionService? = null) : GenericConverter {

    override fun getConvertibleTypes(): Set<GenericConverter.ConvertiblePair>? {
        return setOf(GenericConverter.ConvertiblePair(Collection::class.java, Collection::class.java))
    }

    @Suppress("UNCHECKED_CAST")
    override fun <S, T> convert(source: Any?, sourceType: Class<S>, targetType: Class<T>): T? {
        if (source == null) {
            return null
        }
        val result = if (targetType.isInterface) {
            if (targetType == List::class.java) {
                ArrayList()
            } else if (targetType == Set::class.java || targetType == Collection::class.java) {
                LinkedHashSet()
            } else if (targetType == SortedSet::class.java) {
                TreeSet()
            } else {
                throw IllegalStateException("不支持转换成为这样的集合类型[type=$targetType]")
            }
            // 不是接口，直接反射创建目标对象...
        } else {
            ClassUtils.newInstance(targetType) as MutableCollection<Any?>
        }
        if (source is Collection<*>) {
            (source as Collection<Any?>).forEach(result::add)
        }
        return result as T?
    }
}