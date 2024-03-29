package com.wanna.boot.context.properties.bind

import com.wanna.boot.convert.ApplicationConversionService
import com.wanna.framework.core.ResolvableType
import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.convert.TypeDescriptor
import com.wanna.framework.core.convert.converter.GenericConverter
import com.wanna.framework.core.convert.support.GenericConversionService
import com.wanna.framework.lang.Nullable
import java.util.*

/**
 * 绑定的类型转换器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/3
 *
 * @param conversionServices 提供类型转换的ConversionService列表
 */
class BindConverter(conversionServices: List<ConversionService>) {
    /**
     * ConversionService列表
     */
    private val delegates: List<ConversionService>

    init {
        val delegates = ArrayList<ConversionService>()
        // 1.检查一下给定的ConversionServices当中是否有ApplicationConversionService
        var hasApplication = false

        // 将全部的ConversionService全部去进行merge, 并统计一下hasApplication标志
        if (conversionServices.isNotEmpty()) {
            conversionServices.forEach {
                delegates += it
                hasApplication = hasApplication || it is ApplicationConversionService
            }
        }

        // 2.如果不存在有ApplicationConversionService的话, 那么再加入一个ApplicationConversionService...
        if (conversionServices.isEmpty()) {
            delegates += ApplicationConversionService.getSharedInstance()
        }

        this.delegates = Collections.unmodifiableList(delegates)
    }

    /**
     * 将给定对象去转换成为目标类型
     *
     * @param source source原始的待转换的对象
     * @param target 要将目标对象去转换成为什么类型?
     * @return 转换之后得到的对象
     */
    @Nullable
    fun <T : Any> convert(@Nullable source: Any?, target: Bindable<T>): T? {
        source ?: return null
        return convert(source, target.type, target.annotations)
    }

    /**
     * 将给定的对象去转换成为目标类型
     *
     * @param source 原始的待转换的对象
     * @param targetType 要去进行转换的目标类型
     * @return 转换之后得到的对象
     */
    @Nullable
    fun <T : Any> convert(source: Any, targetType: ResolvableType): T? {
        return convert(source, targetType, emptyArray())
    }

    /**
     * 将给定的对象去转换成为目标类型
     *
     * @param source 原始的待转换的对象
     * @param targetType 要去进行转换的目标类型
     * @param annotations target身上的注解列表
     * @return 转换之后得到的对象
     */
    @Nullable
    fun <T : Any> convert(source: Any, targetType: ResolvableType, annotations: Array<Annotation>): T? {
        return convert(source, TypeDescriptor.forObject(source), ResolvableTypeDescriptor(targetType, annotations))
    }

    /**
     * 检查给定的对象, 是否能转换成为目标类型的对象?
     *
     * @param source 待检查的对象实例
     * @param targetType 需要去进行转换的目标类型
     * @return 能否去进行转换?
     */
    fun canConvert(@Nullable source: Any?, targetType: ResolvableType): Boolean {
        source ?: return false
        delegates.forEach {
            if (it.canConvert(TypeDescriptor.forObject(source), TypeDescriptor(targetType))) {
                return true
            }
        }
        return false
    }

    @Nullable
    @Suppress("UNCHECKED_CAST")
    private fun <T : Any> convert(source: Any, sourceType: TypeDescriptor, targetType: TypeDescriptor): T? {
        delegates.forEach {
            if (it.canConvert(sourceType, targetType)) {
                return it.convert(source, targetType) as T?
            }
        }
        return null
    }

    private class ResolvableTypeDescriptor(type: ResolvableType, val annotations: Array<Annotation>) :
        TypeDescriptor(type)

    /**
     * TypeConverter ConversionService
     */
    private class TypeConverterConversionService : GenericConversionService() {
        init {
            addConverter(TypeConverterConverter())
        }
    }

    @Suppress("UNCHECKED_CAST")
    private class TypeConverterConverter : GenericConverter {
        override fun getConvertibleTypes(): Set<GenericConverter.ConvertiblePair> = setOf(
            GenericConverter.ConvertiblePair(String::class.java, Any::class.java)
        )

        override fun <S : Any, T : Any> convert(source: Any?, sourceType: Class<S>, targetType: Class<T>): T? {
            return source as T?
        }

        override fun convert(source: Any?, sourceType: TypeDescriptor, targetType: TypeDescriptor): Any? {
            return source
        }
    }

    companion object {
        /**
         * 基于给定的[ConversionService], 快速去构建出来[BindConverter]的工厂方法
         *
         * @param conversionServices ConversionService列表
         * @return BindConverter
         */
        @JvmStatic
        fun get(conversionServices: List<ConversionService>): BindConverter {
            return BindConverter(conversionServices)
        }
    }
}