package com.wanna.framework.beans.factory.support.definition.config

/**
 * 这是一个BeanMetadata的属性
 */
open class BeanMetadataAttribute(val name: String?, val value: Any?) : BeanMetadataElement {

    private var source: Any? = null;

    fun setSource(source: Any?) {
        this.source = source;
    }

    override fun getSource(): Any? {
        return this.source
    }
}