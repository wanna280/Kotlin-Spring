package com.wanna.framework.beans.definition

/**
 * 这是一个被注解标注的通用的BeanDefinition
 */
open class AnnotatedGenericBeanDefinition(_beanClass: Class<*>) : AnnotatedBeanDefinition,
    GenericBeanDefinition(_beanClass) {

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