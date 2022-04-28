package com.wanna.framework.beans.method

import com.wanna.framework.beans.factory.support.definition.config.BeanMetadataAttributeAccessor

/**
 * 描述的是一个对象的属性值
 */
open class PropertyValue(val name: String, val value: Any?) : BeanMetadataAttributeAccessor() {

    override fun toString(): String {
        return "PropertyValue(name='$name', value=$value)"
    }
}