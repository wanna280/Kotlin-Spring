package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.MutablePropertyValues
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.config.BeanMetadataElement

/**
 * 支持去解析Bean引用的列表，支持去添加一个RuntimeBeanReference列表交给Spring去进行解析
 *
 * @see com.wanna.framework.beans.factory.config.RuntimeBeanReference
 * @see BeanDefinitionValueResolver.resolveValueIfNecessary
 * @see BeanDefinition.getPropertyValues
 * @see MutablePropertyValues.addPropertyValue
 */
open class ManagedList<T> : ArrayList<T>(), BeanMetadataElement {

    // source
    private var source: Any? = null

    override fun getSource() = this.source

    open fun setSource(source: Any?) {
        this.source = source
    }
}