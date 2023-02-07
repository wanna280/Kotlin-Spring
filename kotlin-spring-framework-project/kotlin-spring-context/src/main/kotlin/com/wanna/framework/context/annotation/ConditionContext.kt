package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.io.ResourceLoader
import javax.annotation.Nullable

/**
 * 为[Condition]去进行条件匹配时, 需要用到的上下文环境信息
 *
 * @see Conditional
 * @see Condition
 */
interface ConditionContext {

    /**
     * 获取[BeanDefinitionRegistry]
     *
     * @return BeanDefinitionRegistry
     */
    fun getRegistry(): BeanDefinitionRegistry


    /**
     * 获取[ConfigurableListableBeanFactory]
     *
     * @return BeanFactory
     */
    @Nullable
    fun getBeanFactory(): ConfigurableListableBeanFactory?

    /**
     * 获取[Environment]
     *
     * @return Environment
     */
    fun getEnvironment(): Environment

    /**
     * 获取[ClassLoader]
     *
     * @return BeanClassLoader
     */
    fun getClassLoader(): ClassLoader

    /**
     * 获取到用于资源加载的[ResourceLoader]
     *
     * @return ResourceLoader
     */
    fun getResourceLoader(): ResourceLoader
}