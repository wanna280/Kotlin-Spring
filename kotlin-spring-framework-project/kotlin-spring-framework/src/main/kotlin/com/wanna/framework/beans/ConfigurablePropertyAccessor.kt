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
     * 对于当前的[PropertyAccessor], 是否需自增内部嵌套的路径?
     */
    var autoGrowNestedPaths: Boolean

    /**
     * 是否需要提取一个属性的旧值给PropertyEditor?
     */
    var extractOldValueForEditor: Boolean

    /**
     * 获取ConversionService
     *
     * @return ConversionService, if any
     */
    @Nullable
    fun getConversionService(): ConversionService?

    /**
     * 设置ConversionService
     *
     * @param conversionService ConversionService
     */
    fun setConversionService(@Nullable conversionService: ConversionService?)
}