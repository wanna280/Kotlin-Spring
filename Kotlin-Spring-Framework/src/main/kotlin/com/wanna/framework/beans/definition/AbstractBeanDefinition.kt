package com.wanna.framework.beans.definition

/**
 * 这是一个抽象的BeanDefinition
 */
abstract class AbstractBeanDefinition(override var beanClass:Class<*>) : BeanDefinition {

    override fun isSingleton(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isPrototype(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isFactoryBean(): Boolean {
        TODO("Not yet implemented")
    }
}