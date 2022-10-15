package com.wanna.framework.beans.factory.support.definition.config

/**
 * 这是一个Bean的Metadata的属性访问器，目的是为了提供BeanMetadataAttribute的访问
 */
open class BeanMetadataAttributeAccessor : AttributeAccessorSupport() {

    private var source: Any? = null

    fun getSource(): Any? {
        return source
    }

    fun setSource(source: Any?) {
        this.source = source
    }

    /**
     * 添加一个属性
     */
    fun addAttribute(name: String, attribute: BeanMetadataAttribute) {
        super.setAttribute(name, attribute)
    }

    /**
     * 获取一个Metadata的Attribute
     */
    fun getMetadataAttribute(name: String): BeanMetadataAttribute {
        return super.getAttribute(name) as BeanMetadataAttribute
    }

    override fun getAttribute(name: String): Any? {
        val attribute = super.getAttribute(name)
        return if (attribute == null) null else (attribute as BeanMetadataAttribute).value
    }

    override fun setAttribute(name: String, value: Any?) {
        super.setAttribute(name, BeanMetadataAttribute(name, value))
    }

    override fun removeAttribute(name: String): Any? {
        val attribute = super.removeAttribute(name)
        return if (attribute == null) null else (attribute as BeanMetadataAttribute).value
    }

}