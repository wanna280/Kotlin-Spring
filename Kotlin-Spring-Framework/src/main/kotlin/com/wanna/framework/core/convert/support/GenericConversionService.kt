package com.wanna.framework.core.convert.support

import com.wanna.framework.core.convert.converter.Converter
import com.wanna.framework.core.convert.converter.GenericConverter
import com.wanna.framework.core.convert.converter.GenericConverter.ConvertiblePair
import java.util.concurrent.CopyOnWriteArraySet

/**
 * 这是一个通用的ConversionService
 */
open class GenericConversionService : ConfigurableConversionService {

    // 已经注册的Converter列表
    private val converters = Converters()

    /**
     * 遍历所有的Converter，挨个去判断能否进行转换？
     */
    override fun canConvert(sourceType: Class<*>, targetType: Class<*>): Boolean {
        val globalConverters = converters.globalConverters
        globalConverters.forEach {
            val convertibleTypes = it.getConvertibleTypes()
            if (convertibleTypes != null && convertibleTypes.contains(ConvertiblePair(sourceType, targetType))) {
                return true
            }
        }
        return false
    }

    override fun <T> convert(source: Any?, targetType: Class<T>): T? {
        if (source == null) {
            return null
        }
        val globalConverters = converters.globalConverters
        val sourceType = source::class.java
        globalConverters.forEach {
            val convertibleTypes = it.getConvertibleTypes()
            if (convertibleTypes != null && convertibleTypes.contains(ConvertiblePair(sourceType, targetType))) {
                return it.convert(source, sourceType, targetType)
            }
        }
        return null
    }

    override fun addConverter(converter: Converter<*, *>) {
        addConverter(ConverterAdapter(converter))
    }

    override fun <S, T> addConverter(sourceType: Class<S>, targetType: Class<T>, converter: Converter<S, T>) {
        val adapter = ConverterAdapter(converter)
        adapter.addConvertibleType(sourceType, targetType)
        addConverter(adapter)
    }

    override fun addConverter(converter: GenericConverter) {
        converters.addConverter(converter)
    }

    private class Converters {
        // 全局的Converter列表
        val globalConverters = CopyOnWriteArraySet<GenericConverter>()

        /**
         * 添加Converter
         */
        fun addConverter(converter: GenericConverter) {
            globalConverters += converter
        }
    }

    @Suppress("UNCHECKED_CAST")
    private class ConverterAdapter(private val converter: Converter<*, *>) : GenericConverter {
        private val convertibleTypes = HashSet<ConvertiblePair>()

        /**
         * 添加可以转换的类型列表
         * @param sourceType 源类型
         * @param targetType 目标类型
         */
        fun addConvertibleType(sourceType: Class<*>, targetType: Class<*>) {
            this.convertibleTypes.add(ConvertiblePair(sourceType, targetType))
        }

        override fun getConvertibleTypes(): Set<ConvertiblePair> {
            return convertibleTypes
        }

        override fun <S, T> convert(source: Any?, sourceType: Class<S>, targetType: Class<T>): T? {
            return converter.convert(source as Nothing?) as T?
        }
    }
}