package com.wanna.framework.beans

import com.wanna.framework.beans.propertyeditors.CharsetEditor
import com.wanna.framework.beans.propertyeditors.ClassArrayEditor
import com.wanna.framework.beans.propertyeditors.ClassEditor
import com.wanna.framework.beans.propertyeditors.UUIDEditor
import com.wanna.framework.constants.CLASS_ARRAY_TYPE
import com.wanna.framework.core.convert.ConversionService
import java.beans.PropertyEditor
import java.nio.charset.Charset
import java.util.UUID

/**
 * 提供PropertyEditorRegistry的模板方法实现，通过组合PropertyEditor和ConversionService，去提供类型的转换工作
 */
open class PropertyEditorRegistrySupport : PropertyEditorRegistry {

    // ConversionService
    private var conversionService: ConversionService? = null

    // 默认的PropertyEditor列表
    var defaultEditors: MutableMap<Class<*>, PropertyEditor>? = null

    // 自定义的Editor列表
    var customEditors: MutableMap<Class<*>, PropertyEditor>? = null

    init {
        createDefaultEditors()
    }

    override fun registerCustomEditor(requiredType: Class<*>, propertyEditor: PropertyEditor) {
        var customEditors = this.customEditors
        if (customEditors == null) {
            customEditors = LinkedHashMap(16)
            this.customEditors = customEditors
        }
        customEditors[requiredType] = propertyEditor
    }

    override fun findCustomEditor(requiredType: Class<*>): PropertyEditor? {
        return customEditors?.get(requiredType)
    }

    /**
     * 创建使用到的默认的PropertyEditor
     */
    private fun createDefaultEditors() {
        this.defaultEditors = LinkedHashMap()

        val defaultEditors = this.defaultEditors!!
        defaultEditors[Class::class.java] = ClassEditor()
        defaultEditors[Charset::class.java] = CharsetEditor()
        defaultEditors[CLASS_ARRAY_TYPE] = ClassArrayEditor()

        defaultEditors[UUID::class.java] = UUIDEditor()
    }


    open fun getConversionService() : ConversionService? {
        return this.conversionService
    }

    open fun setConversionService(conversionService: ConversionService?) {
        this.conversionService = conversionService
    }
}