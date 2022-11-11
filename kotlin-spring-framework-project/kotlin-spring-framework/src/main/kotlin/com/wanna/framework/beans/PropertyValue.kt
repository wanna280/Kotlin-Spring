package com.wanna.framework.beans

import com.wanna.framework.beans.factory.support.definition.config.BeanMetadataAttributeAccessor

/**
 * 描述的是一个对象的属性值
 *
 * @param name propertyName
 * @param value propertyValue
 */
open class PropertyValue(val name: String, var value: Any?) :
    BeanMetadataAttributeAccessor() {

    // 该PropertyValue是否是可选的？
    var optional: Boolean = true

    override fun toString() = "PropertyValue(name='$name', value=$value)"
}