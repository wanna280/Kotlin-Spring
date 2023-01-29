package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.io.ResourceLoader

/**
 * 这是一个用来判断Condition的相关上下文环境
 *
 * @see Conditional
 * @see Condition
 */
interface ConditionContext {

    fun getRegistry(): BeanDefinitionRegistry

    fun getBeanFactory(): ConfigurableListableBeanFactory?

    fun getEnvironment(): Environment

    fun getClassLoader() : ClassLoader

    fun getResourceLoader() : ResourceLoader
}