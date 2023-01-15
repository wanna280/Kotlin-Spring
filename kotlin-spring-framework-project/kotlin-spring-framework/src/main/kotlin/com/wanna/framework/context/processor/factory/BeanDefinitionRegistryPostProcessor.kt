package com.wanna.framework.context.processor.factory

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry

/**
 * 这是一个BeanFactoryPostProcessor的扩展接口, 在原有的[BeanFactoryPostProcessor]的功能之上,
 * 新增对于BeanDefinitionRegistry的后置处理功能, 例如可以对BeanDefinitionRegistry去进行处理
 *
 * @see BeanFactoryPostProcessor
 * @see com.wanna.framework.context.processor.factory.internal.ConfigurationClassPostProcessor
 */
interface BeanDefinitionRegistryPostProcessor : BeanFactoryPostProcessor {

    /**
     * 对BeanDefinitionRegistry去进行后置处理, 例如可以往里面注册BeanDefinition
     *
     * @param registry BeanDefinitionRegistry
     */
    fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry)
}