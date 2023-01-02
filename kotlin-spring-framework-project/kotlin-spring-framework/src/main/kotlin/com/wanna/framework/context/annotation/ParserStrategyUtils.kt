package com.wanna.framework.context.annotation

import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.context.ResourceLoaderAware
import com.wanna.framework.context.aware.BeanClassLoaderAware
import com.wanna.framework.context.aware.EnvironmentAware
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.io.ResourceLoader
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.BeanUtils
import com.wanna.framework.util.ClassUtils

/**
 * 这是一个解析器策略的工具类，它可以完成Bean的实例化，
 * 并回调相关的Aware接口(执行Aware接口，目前支持Environment/ClassLoader/BeanFactory的注入)
 *
 * @see BeanFactoryAware
 * @see ResourceLoaderAware
 * @see EnvironmentAware
 */
@Suppress("UNCHECKED_CAST")
object ParserStrategyUtils {
    /**
     * 根据beanClass, 去实例化得到BeanInstance，并完成Aware接口的回调
     *
     * @param clazz beanClass
     * @param environment Environment
     * @param registry BeanDefinitionRegistry
     * @param resourceLoader ResourceLoader
     */
    @JvmStatic
    fun <T> instanceClass(
        clazz: Class<*>,
        @Nullable environment: Environment?,
        @Nullable registry: BeanDefinitionRegistry?,
        @Nullable resourceLoader: ResourceLoader?
    ): T {
        // 获取classLoader
        val classLoader =
            if (registry is ConfigurableListableBeanFactory) registry.getBeanClassLoader() else ClassUtils.getDefaultClassLoader()
        // 使用BeanUtils去实例化对象
        val instance = createInstance(clazz, environment, registry, resourceLoader, classLoader)
        // 执行Aware接口中的方法
        invokeAwareMethods(instance, environment, registry, classLoader, resourceLoader)
        return instance as T
    }

    /**
     * 创建BeanInstance
     */
    @JvmStatic
    private fun createInstance(
        clazz: Class<*>,
        @Nullable environment: Environment?,
        @Nullable registry: BeanDefinitionRegistry?,
        @Nullable resourceLoader: ResourceLoader?,
        @Nullable classLoader: ClassLoader?
    ): Any {
        return BeanUtils.instantiateClass(clazz)
    }

    /**
     * 执行Aware接口，目前支持Environment/ClassLoader/BeanFactory/ResourceLoader的注入
     *
     * @param bean bean
     * @param environment Environment
     * @param registry BeanDefinitionRegistry
     * @param classLoader ClassLoader
     * @param resourceLoader ResourceLoader
     */
    @JvmStatic
    private fun invokeAwareMethods(
        bean: Any,
        @Nullable environment: Environment?,
        @Nullable registry: BeanDefinitionRegistry?,
        @Nullable classLoader: ClassLoader?,
        @Nullable resourceLoader: ResourceLoader?
    ) {
        if (bean is ResourceLoaderAware && resourceLoader != null) {
            bean.setResourceLoader(resourceLoader)
        }
        if (bean is EnvironmentAware && environment != null) {
            bean.setEnvironment(environment)
        }
        if (bean is BeanClassLoaderAware && classLoader != null) {
            bean.setBeanClassLoader(classLoader)
        }
        if (bean is BeanFactoryAware && registry is BeanFactory) {
            bean.setBeanFactory(registry)
        }
    }
}