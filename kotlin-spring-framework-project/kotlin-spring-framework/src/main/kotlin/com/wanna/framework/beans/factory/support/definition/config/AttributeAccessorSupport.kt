package com.wanna.framework.beans.factory.support.definition.config

import com.wanna.framework.lang.Nullable

/**
 * 这个类用来提供属性访问的支持
 */
open class AttributeAccessorSupport : AttributeAccessor {

    private val attributes = LinkedHashMap<String, Any?>()

    override fun setAttribute(name: String, @Nullable value: Any?) {
        attributes[name] = value
    }

    @Nullable
    override fun getAttribute(name: String) = attributes[name]

    override fun hasAttribute(name: String) = attributes.containsKey(name)

    override fun attributeNames() = attributes.keys.toTypedArray()

    override fun removeAttribute(name: String) = attributes.remove(name)
}