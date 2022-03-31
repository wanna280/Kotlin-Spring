package com.wanna.framework.beans

/**
 * 这是一个BeanDefinition的实现
 * @see BeanDefinition
 */
open class RootBeanDefinition(override var beanName: String, override var beanClass: Class<*>) : BeanDefinition {

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