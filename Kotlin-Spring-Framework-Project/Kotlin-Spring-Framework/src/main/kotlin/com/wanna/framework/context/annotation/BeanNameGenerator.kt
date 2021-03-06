package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry

/**
 * 这是一个BeanName的生成器，是一个典型的策略接口；
 * 给定BeanDefinition和BeanDefinitionRegistry，它可以去对指定的beanDefinition去进行beanName的生成
 *
 * @see AnnotationBeanNameGenerator
 * @see FullyQualifiedAnnotationBeanNameGenerator
 */
interface BeanNameGenerator {
    fun generateBeanName(beanDefinition: BeanDefinition, registry: BeanDefinitionRegistry): String
}