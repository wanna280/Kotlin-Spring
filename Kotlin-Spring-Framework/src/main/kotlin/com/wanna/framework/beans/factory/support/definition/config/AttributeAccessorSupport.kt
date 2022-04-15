package com.wanna.framework.beans.factory.support.definition.config

/**
 * 这个类用来提供属性访问的支持
 */
open class AttributeAccessorSupport : AttributeAccessor {

    private val attributes = LinkedHashMap<String, Any?>()

    override fun setAttribute(name: String, value: Any?) {
        attributes[name] = value
    }

    override fun getAttribute(name: String): Any? {
        return attributes[name]
    }

    override fun hasAttribute(name: String): Boolean {
        return attributes.containsKey(name)
    }

    override fun attributeNames(): Array<String> {
        return attributes.keys.toTypedArray()
    }

    override fun removeAttribute(name: String): Any? {
        return attributes.remove(name)
    }
}