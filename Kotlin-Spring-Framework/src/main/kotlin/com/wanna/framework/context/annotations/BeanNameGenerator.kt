package com.wanna.framework.context.annotations

import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.context.BeanDefinitionRegistry

/**
 * 这是一个BeanName的生成器，决定BeanName
 */
interface BeanNameGenerator {
    fun generateBeanName(beanDefinition: BeanDefinition, registry: BeanDefinitionRegistry) : String
}