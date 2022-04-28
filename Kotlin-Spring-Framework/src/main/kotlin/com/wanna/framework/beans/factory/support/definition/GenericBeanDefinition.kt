package com.wanna.framework.beans.factory.support.definition

/**
 * 这是一个通用的BeanDefinition，它主要为parent的BeanDefinition的动态设置提供支持
 */
open class GenericBeanDefinition(_beanClass: Class<*>?) : AbstractBeanDefinition(_beanClass) {

    // 提供无参数构造器
    constructor() : this(null)

    // parent BeanDefinition的名字
    private var parent: String? = null

    open fun setParent(parent: String?) {
        this.parent = parent
    }

    open fun getParent(): String? {
        return this.parent
    }
}