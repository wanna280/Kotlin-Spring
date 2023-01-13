package com.wanna.framework.beans

import com.wanna.framework.beans.factory.support.definition.config.BeanMetadataAttributeAccessor
import com.wanna.framework.lang.Nullable

/**
 * 描述的是一个对象的属性值
 *
 * @param name propertyName
 * @param value propertyValue
 */
open class PropertyValue(val name: String, @Nullable var value: Any?) :
    BeanMetadataAttributeAccessor() {

    /**
     * 该PropertyValue是否是可选的？
     */
    var optional: Boolean = true

    /**
     * 已经解析完成的Tokens
     */
    @Nullable
    var resolvedTokens: Any? = null

    override fun toString() = "PropertyValue(name='$name', value=$value)"
}