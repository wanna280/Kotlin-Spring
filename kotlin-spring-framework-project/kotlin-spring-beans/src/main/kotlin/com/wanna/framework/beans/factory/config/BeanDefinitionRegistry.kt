package com.wanna.framework.beans.factory.config

import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.exception.NoSuchBeanDefinitionException

/**
 * 这是一个BeanDefinition的注册中心, 它负责管理BeanDefinition的注册、删除
 */
interface BeanDefinitionRegistry {
    /**
     * 注册一个BeanDefinition到当前的BeanFactory当中来
     *
     * @param name beanName
     * @param beanDefinition 需要去进行注册的BeanDefinition
     */
    fun registerBeanDefinition(name: String, beanDefinition: BeanDefinition)

    /**
     * 根据beanName去移除一个指定的BeanDefinition
     *
     * @param name 需要移除BeanDefinition的beanName
     * @throws NoSuchBeanDefinitionException 如果BeanDefinitionRegistry当中都不包含这个beanName的BeanDefinition的话
     */
    @Throws(NoSuchBeanDefinitionException::class)
    fun removeBeanDefinition(name: String)

    /**
     * 获取所有的已经注册的BeanDefinition的beanName列表
     *
     * @return 当前BeanDefinitionRegistry当中已经注册的BeanDefinition的beanName列表
     */
    fun getBeanDefinitionNames(): List<String>

    /**
     * 获取当前BeanDefinitionRegistry已经注册的BeanDefinition的数量
     *
     * @return BeanFactory当中已经注册的BeanDefinition的数量
     */
    fun getBeanDefinitions(): List<BeanDefinition>

    /**
     * 获取BeanDefinition, 一定能获取到, 如果获取不到直接抛出异常;
     * 如果想要不抛出异常, 请先使用[containsBeanDefinition]方法去进行判断该BeanDefinition是否存在
     *
     * @param beanName beanName
     * @return BeanDefinition
     * @see containsBeanDefinition
     * @throws NoSuchBeanDefinitionException 如果没有找到这样的BeanDefinition异常
     */
    @Throws(NoSuchBeanDefinitionException::class)
    fun getBeanDefinition(beanName: String): BeanDefinition

    /**
     * 检查当前BeanDefinitionRegistry当中是否包含了给定的beanName的BeanDefinition
     *
     * @param name beanName
     * @return 如果包含了该BeanDefinition, 那么return true; 否则return false
     */
    fun containsBeanDefinition(name: String): Boolean

    /**
     * 获取当前BeanDefinitionRegistry当中的BeanDefinition的数量
     *
     * @return count of BeanDefinition
     */
    fun getBeanDefinitionCount(): Int
}