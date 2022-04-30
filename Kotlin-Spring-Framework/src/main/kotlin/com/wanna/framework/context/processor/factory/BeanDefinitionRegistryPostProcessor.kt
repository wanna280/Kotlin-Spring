package com.wanna.framework.context.processor.factory

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry

/**
 * 这是一个BeanFactoryPostProcessor的扩展接口，可以对BeanDefinitionRegistry去进行处理
 */
interface BeanDefinitionRegistryPostProcessor : BeanFactoryPostProcessor {

    /**
     * 对BeanDefinitionRegistry去进行后置处理，例如可以往里面注册BeanDefinition
     */
    fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry)
}