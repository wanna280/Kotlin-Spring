package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.core.io.Resource
import com.wanna.framework.core.io.ResourceLoader
import com.wanna.framework.lang.Nullable

/**
 * BeanDefinitionReader, 提供对于BeanDefinition的加载功能
 */
interface BeanDefinitionReader {

    /**
     * 获取BeanDefinitionRegistry
     *
     * @return BeanDefinitionRegistry
     */
    fun getRegistry(): BeanDefinitionRegistry

    /**
     * 获取资源加载器
     *
     * @return ResourceLoader(如果没有的话, return null)
     */
    @Nullable
    fun getResourceLoader(): ResourceLoader?

    /**
     * 获取BeanClassLoader
     *
     * @return BeanClassLoader(如果没有的话, return null)
     */
    @Nullable
    fun getBeanClassLoader(): ClassLoader?

    /**
     * 获取BeanNameGenerator
     *
     * @return BeanNameGenerator
     */
    fun getBeanNameGenerator(): BeanNameGenerator

    /**
     * 加载BeanDefinition
     *
     * @param resource Resource
     * @return 加载到的BeanDefinition数量
     */
    fun loadBeanDefinitions(resource: Resource): Int

    /**
     * 加载BeanDefinition
     *
     * @param resources Resource列表
     * @return 加载到的BeanDefinition数量
     */
    fun loadBeanDefinitions(vararg resources: Resource): Int

    /**
     * 加载BeanDefinition
     *
     * @param location 资源路径
     * @return 加载到的BeanDefinition数量
     */
    fun loadBeanDefinitions(location: String): Int

    /**
     * 加载BeanDefinition
     *
     * @param locations 资源路径
     * @return 加载到的BeanDefinition数量
     */
    fun loadBeanDefinitions(vararg locations: String): Int
}