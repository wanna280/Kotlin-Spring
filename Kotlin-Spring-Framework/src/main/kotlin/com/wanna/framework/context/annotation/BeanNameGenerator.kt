package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry

/**
 * 这是一个BeanName的生成器，它可以去决定BeanName的生成
 */
interface BeanNameGenerator {
    fun generateBeanName(beanDefinition: BeanDefinition, registry: BeanDefinitionRegistry) : String
}