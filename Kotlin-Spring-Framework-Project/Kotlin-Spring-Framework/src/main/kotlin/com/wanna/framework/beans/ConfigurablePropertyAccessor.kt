package com.wanna.framework.beans

import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.lang.Nullable


/**
 * 它是一个可以支持配置的[PropertyAccessor]
 *
 * 它通过集成[PropertyEditorRegistry]和[ConversionService]，去实现支持去进行属性值去进行按照指定的类型去进行设置的功能；
 *
 * @see PropertyEditorRegistry
 */
interface ConfigurablePropertyAccessor : PropertyAccessor, PropertyEditorRegistry, TypeConverter {

    /**
     * 设置ConversionService，为提供类型的转换提供支持
     *
     * @param conversionService ConversionService
     */
    fun setConversionService(@Nullable conversionService: ConversionService?)

    /**
     * 获取ConversionService
     *
     * @return ConversionService
     */
    @Nullable
    fun getConversionService(): ConversionService?
}