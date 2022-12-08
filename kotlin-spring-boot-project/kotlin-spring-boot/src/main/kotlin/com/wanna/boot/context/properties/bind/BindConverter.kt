package com.wanna.boot.context.properties.bind

import com.wanna.boot.convert.ApplicationConversionService
import com.wanna.framework.core.ResolvableType
import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.convert.TypeDescriptor
import com.wanna.framework.core.convert.converter.GenericConverter
import com.wanna.framework.core.convert.support.GenericConversionService
import com.wanna.framework.lang.Nullable

/**
 * 绑定的类型转换器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/3
 */
class BindConverter {

    companion object {

        /**
         * 基于给定的[ConversionService], 快速去构建出来[BindConverter]的工厂方法
         *
         * @param conversionServices ConversionService列表
         * @return BindConverter
         */
        @JvmStatic
        fun get(conversionServices: List<ConversionService>): BindConverter {
            return BindConverter()
        }
    }


    /**
     * ConversionService列表
     */
    private val delegates = arrayOf(ApplicationConversionService.getSharedInstance())

    /**
     * 将给定对象去转换成为目标类型
     *
     * @param source source原始的待转换的对象
     * @param target 要将目标对象去转换成为什么类型?
     * @return 转换之后得到的对象
     */
    @Nullable
    fun <T : Any> convert(source: Any?, target: Bindable<T>): T? {
        source ?: return null
        return convert(source, target.type, target.annotations)
    }

    @Nullable
    fun <T : Any> convert(source: Any, targetType: ResolvableType): T? {
        return convert<T>(source, TypeDescriptor.forObject(source), ResolvableTypeDescriptor(targetType, emptyArray()))
    }

    @Nullable
    fun <T : Any> convert(source: Any, targetType: ResolvableType, annotations: Array<Annotation>): T? {
        return convert(source, TypeDescriptor.forObject(source), ResolvableTypeDescriptor(targetType, annotations))
    }

    /**
     * 检查给定的对象, 是否能转换成为目标对象
     *
     * @param source 待检查的对象实例
     * @param targetType 需要去进行转换的目标类型
     * @return 能否去进行转换?
     */
    fun canConvert(source: Any?, targetType: ResolvableType): Boolean {
        source ?: return false
        delegates.forEach {
            if (it.canConvert(TypeDescriptor.forClass(source::class.java), TypeDescriptor(targetType))) {
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
}