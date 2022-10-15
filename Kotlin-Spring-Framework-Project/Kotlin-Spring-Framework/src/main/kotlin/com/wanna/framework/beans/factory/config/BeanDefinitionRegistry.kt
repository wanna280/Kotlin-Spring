package com.wanna.framework.beans.factory.config

import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.context.exception.NoSuchBeanDefinitionException

/**
 * 这是一个BeanDefinition的注册中心，它负责管理BeanDefinition的注册
 */
interface BeanDefinitionRegistry {
    /**
     * 往注册中心当中其注册一个BeanDefinition
     */
    fun registerBeanDefinition(name: String, beanDefinition: BeanDefinition)

    /**
     * 根据name从注册中心当中去移除一个BeanDefinition
     */
    fun removeBeanDefinition(name: String)

    /**
     * 获取BeanDefinitioNames
     */
    fun getBeanDefinitionNames(): List<String>

    /**
     * 获取BeanDefinition列表
     */
    fun getBeanDefinitions(): List<BeanDefinition>

    /**
     * 获取BeanDefinition，一定能获取到，如果获取不到直接抛出异常；
     * 如果想要不抛出异常，请先使用containsBeanDefinition去进行判断该BeanDefinition是否存在
     *
     * @throws NoSuchBeanDefinitionException 如果没有找到这样的BeanDefinition异常
     * @see containsBeanDefinition
     */
    fun getBeanDefinition(beanName: String): BeanDefinition

    /**
     * 是否注册了这个BeanDefinition？
     */
    fun containsBeanDefinition(name: String): Boolean

    /**
     * 获取BeanDefinition的数量
     */
    fun getBeanDefinitionCount() : Int
}