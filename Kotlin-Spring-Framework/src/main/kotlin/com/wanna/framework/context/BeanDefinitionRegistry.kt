package com.wanna.framework.context

import com.wanna.framework.beans.factory.support.definition.BeanDefinition

/**
 * 这是一个BeanDefinition的注册中心，它负责管理BeanDefinition的注册
 */
interface BeanDefinitionRegistry {
    /**
     * 往注册中心当中其注册一个BeanDefinition
     */
    fun registerBeanDefinition(name: String, beanDefinition: BeanDefinition)

    /**
     * 获取BeanDefinitioNames
     */
    fun getBeanDefinitionNames(): List<String>

    /**
     * 获取BeanDefinition列表
     */
    fun getBeanDefinitions(): List<BeanDefinition>

    /**
     * 获取BeanDefinition
     */
    fun getBeanDefinition(beanName: String): BeanDefinition?

    /**
     * 是否注册了这个BeanDefinition？
     */
    fun containsBeanDefinition(name: String): Boolean

    /**
     * 获取BeanDefinition的数量
     */
    fun getBeanDefinitionCount() : Int
}