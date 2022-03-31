package com.wanna.framework.beans.definition

/**
 * 提供对Bean的相关信息的管理
 */
interface BeanDefinition {

    // beanClass
    var beanClass: Class<*>

    /**
     * 是否单例
     */
    fun isSingleton(): Boolean

    /**
     * 是否原型？
     */
    fun isPrototype(): Boolean

    /**
     * 是否是FactoryBean
     */
    fun isFactoryBean(): Boolean
}