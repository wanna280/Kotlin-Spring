package com.wanna.framework.beans.definition

import com.wanna.framework.beans.definition.BeanDefinition

/**
 * 这是一个BeanDefinition的实现
 * @see BeanDefinition
 */
open class RootBeanDefinition(_beanClass: Class<*>) : AbstractBeanDefinition(_beanClass) {

    override fun isSingleton(): Boolean {
        return true
    }

    override fun isPrototype(): Boolean {
        return false
    }

    override fun isFactoryBean(): Boolean {
        return false
    }
}