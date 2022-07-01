package com.wanna.framework.validation

import com.wanna.framework.beans.PropertyEditorRegistry
import com.wanna.framework.beans.SimpleTypeConverter
import com.wanna.framework.beans.TypeConverter
import com.wanna.framework.beans.TypeConverterSupport
import com.wanna.framework.core.convert.ConversionService
import com.wanna.framework.core.convert.support.DefaultConversionService
import java.beans.PropertyEditor

/**
 * 数据绑定器，组合了TypeConverter，支持去进行类型的转换工作
 */
open class DataBinder : PropertyEditorRegistry, TypeConverter {

    private val typeConverter = SimpleTypeConverter()

    init {
        typeConverter.setConversionService(DefaultConversionService())
    }

    open fun getTypeConverter(): TypeConverterSupport = this.typeConverter

    override fun registerCustomEditor(requiredType: Class<*>, propertyEditor: PropertyEditor) {
        getTypeConverter().registerCustomEditor(requiredType, propertyEditor)
    }

    override fun findCustomEditor(requiredType: Class<*>): PropertyEditor? {
        return getTypeConverter().findCustomEditor(requiredType)
    }

    override fun <T> convertIfNecessary(value: Any?, requiredType: Class<T>?): T? {
        return getTypeConverter().convertIfNecessary(value, requiredType)
    }

    open fun setConversionService(conversionService: ConversionService) {
        getTypeConverter().setConversionService(conversionService)
    }

    open fun getConversionService(): ConversionService? {
        return getTypeConverter().getConversionService()
    }
}