package com.wanna.framework.beans

import com.wanna.framework.core.convert.ConversionService


/**
 * 它是一个可以支持配置的PropertyAccessor
 *
 * 它通过集成PropertyEditorRegistry和ConversionService，去实现支持去进行属性值去进行按照指定的类型去进行设置的功能；
 *
 * @see PropertyEditorRegistry
 */
interface ConfigurablePropertyAccessor : PropertyAccessor, PropertyEditorRegistry, TypeConverter {

    fun setConversionService(conversionService: ConversionService?)

    fun getConversionService(): ConversionService?
}