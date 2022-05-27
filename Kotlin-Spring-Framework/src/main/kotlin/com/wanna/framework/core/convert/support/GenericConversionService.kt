package com.wanna.framework.core.convert.support

import com.wanna.framework.core.ResolvableType
import com.wanna.framework.core.convert.converter.Converter
import com.wanna.framework.core.convert.converter.GenericConverter
import com.wanna.framework.core.convert.converter.GenericConverter.ConvertiblePair
import com.wanna.framework.core.util.ClassUtils
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.CopyOnWriteArraySet

/**
 * 这是一个通用(带泛型)的ConversionService，它为Converter的注册中心以及可以被配置的ConversionService提供了模板的实现；
 * 它已经可以支持去进行类型的转换，但是它内部并没有添加默认的Converter，也就是说，它本身并不能工作，需要往内部加入Converter，才能完成
 * 类型的转换工作，在DefaultConversion当中，就添加了一些默认的Converter去完成类型转换的处理
 *
 * @see DefaultConversionService
 * @see ConfigurableConversionService
 */
open class GenericConversionService : ConfigurableConversionService {

    // Converter的注册中心，内部维护了全部的Converter的列表
    private val converters = Converters()

    /**
     * 判断Converter注册中心当中，是否存在有这样的Converter，能去完成从sourceType->targetType的类型转换？
     *
     * @param sourceType
     * @param targetType
     * @return 是否能支持从sourceType->targetType？
     */
    override fun canConvert(sourceType: Class<*>, targetType: Class<*>): Boolean {
        val convertersForPair = converters.getConverter(sourceType, targetType)
        return convertersForPair.converters.isNotEmpty()
    }

    /**
     * 遍历ConversionService当中已经注册的所有的Converter，去进行判断，看它是否能进行转换？
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T> convert(source: Any?, targetType: Class<T>): T? {
        if (source == null) {
            return null
        }
        val converter = converters.getConverter(source::class.java, targetType)
        if (converter.converters.isNotEmpty()) {
            return converter.convert(source, targetType) as T?
        }
        return null
    }

    /**
     * 添加一个自定义的Converter，自动去解析Converter的泛型类型去进行注册
     *
     * @param converter 你想要添加的Converter
     * @throws IllegalStateException 如果无法解析出来Converter的泛型类型
     */
    override fun addConverter(converter: Converter<*, *>) {
        // 解析Converter的泛型类型
        val generics = ResolvableType.forClass(converter::class.java).`as`(Converter::class.java).getGenerics()
        if (generics.isEmpty()) {
            throw IllegalStateException("无法解析添加的Converter的泛型类型[$converter]")
        }
        // 将Converter包装成为GenericConverter
        val adapter = ConverterAdapter(converter)
        // 添加该Converter能处理的映射类型...
        adapter.addConvertibleType(generics[0].resolve()!!, generics[1].resolve()!!)
        addConverter(adapter)
    }

    override fun <S, T> addConverter(sourceType: Class<S>, targetType: Class<T>, converter: Converter<S, T>) {
        val adapter = ConverterAdapter(converter)
        adapter.addConvertibleType(sourceType, targetType)
        addConverter(adapter)
    }

    override fun addConverter(converter: GenericConverter) {
        converters.addConverter(converter)
    }

    /**
     * Converter的注册中心
     */
    private class Converters {
        // 全局的Converter列表
        val globalConverters = CopyOnWriteArraySet<GenericConverter>()

        // Converter注册中心当中维护的Converter列表
        // key-(sourceType->targetType)的Pair映射
        // value-能完成Pair映射的Converter列表
        val converters = ConcurrentHashMap<ConvertiblePair, ConvertersForPair>()

        /**
         * 注册Converter，key是ConvertibleType，value是GenericConverter；
         * 将GenericConverter可以转换的类型拿出来作为Key，去完成Mapping->Converters的映射关系注册
         */
        fun addConverter(converter: GenericConverter) {
            converter.getConvertibleTypes()?.forEach {
                val converters = getConverter(it)
                converters.addConverter(converter)
            }
        }

        /**
         * 根据sourceType和targetType去获取Converter
         *
         * @param sourceType sourceType
         * @param targetType targetType
         * @return 支持处理该映射关系的Converter列表
         */
        fun getConverter(sourceType: Class<*>, targetType: Class<*>): ConvertersForPair {
            return getConverter(ConvertiblePair(sourceType, targetType))
        }

        /**
         * 根据(sourceType->targetType)的Pair，去获取到支持处理该种映射方式的Converter；
         * 如果此时注册中心当中还没有这种类型的映射，那么需要新创建一个
         *
         * @return ConvertersForPair，也就是支持处理(sourceType->targetType)这种映射关系的Converter列表
         */
        fun getConverter(type: ConvertiblePair): ConvertersForPair {
            var convertersForPair = this.converters[type]
            if (convertersForPair != null) {
                return convertersForPair
            }
            // 遍历所有的Converter，根据继承关系去进行寻找...
            convertersForPair = find(type)
            val convertersForPairToUse = convertersForPair ?: ConvertersForPair()
            if (convertersForPair == null) {
                this.converters[type] = convertersForPairToUse
            }
            return convertersForPairToUse
        }

        /**
         * 遍历所有的Converter去进行继承关系的匹配，因为有可能给出的是它的子类...
         *
         * @param type 要去进行匹配的sourceType和targetType
         * @return 寻找到的Converters(如果没有找到return null)
         */
        fun find(type: ConvertiblePair): ConvertersForPair? {
            this.converters.forEach { (k, v) ->
                val sourceTypeMatch = ClassUtils.isAssignFrom(k.sourceType, type.sourceType)
                val targetTypeMatch = ClassUtils.isAssignFrom(k.targetType, type.targetType)
                if (sourceTypeMatch && targetTypeMatch) {
                    return v
                }
            }
            return null
        }

        /**
         * 根据(sourceType->targetType)的映射Mapping，去移除掉该Mapping相应的Converter列表
         */
        fun removeConverter(sourceType: Class<*>, targetType: Class<*>) {
            this.converters.remove(ConvertiblePair(sourceType, targetType))
        }
    }

    /**
     * 这是一个映射(Pair,sourceType->targetType的映射)对应的Converter列表的注册中心；
     * 比如一个Integer->String的映射可能会存在有多个Converter都能去进行转换...
     *
     * @see ConvertiblePair
     */
    class ConvertersForPair {
        var converters = ConcurrentLinkedDeque<GenericConverter>()

        fun addConverter(converter: GenericConverter) {
            converters += converter
        }

        fun convert(source: Any, targetType: Class<*>): Any? {
            return converters.first.convert(source, source::class.java, targetType)
        }
    }

    /**
     * 这是一个Converter的Adapter，它可以将普通的Converter转换为GenericConverter去进行包装
     *
     * @param converter 想要去进行包装的Converter
     */
    @Suppress("UNCHECKED_CAST")
    private class ConverterAdapter(private val converter: Converter<*, *>) : GenericConverter {
        // 可以转换的类型映射
        private val convertibleTypes = HashSet<ConvertiblePair>()

        /**
         * 添加可以转换的类型到列表当中(source->target的映射关系)
         *
         * @param sourceType 源类型
         * @param targetType 目标类型
         */
        fun addConvertibleType(sourceType: Class<*>, targetType: Class<*>) {
            this.convertibleTypes.add(ConvertiblePair(sourceType, targetType))
        }

        /**
         * 获取当前Converter支持转换的类型列表
         *
         * @return 当前这个Converter支持的转换的映射列表
         */
        override fun getConvertibleTypes(): Set<ConvertiblePair> {
            return convertibleTypes
        }

        override fun <S, T> convert(source: Any?, sourceType: Class<S>, targetType: Class<T>): T? {
            return converter.convert(source as Nothing?) as T?
        }
    }
}