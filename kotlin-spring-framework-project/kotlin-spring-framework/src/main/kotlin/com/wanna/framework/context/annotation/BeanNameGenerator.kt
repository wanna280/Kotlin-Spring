package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry

/**
 * 这是一个BeanName的生成器, 是一个典型的策略接口;
 * 给定BeanDefinition和BeanDefinitionRegistry, 它可以去对指定的beanDefinition去进行beanName的生成
 *
 * @see AnnotationBeanNameGenerator
 * @see FullyQualifiedAnnotationBeanNameGenerator
 */
fun interface BeanNameGenerator {

    /**
     * 为指定的BeanDefinition去生成beanName
     *
     * @param beanDefinition BeanDefinition
     * @param registry BeanDefinitionRegistry
     * @return 生成得到的beanName
     */
    fun generateBeanName(beanDefinition: BeanDefinition, registry: BeanDefinitionRegistry): String
}