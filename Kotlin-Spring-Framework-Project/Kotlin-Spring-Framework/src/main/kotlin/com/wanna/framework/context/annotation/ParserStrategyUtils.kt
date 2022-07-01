package com.wanna.framework.context.annotation

import com.wanna.framework.beans.BeanFactoryAware
import com.wanna.framework.beans.factory.BeanFactory
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.context.aware.BeanClassLoaderAware
import com.wanna.framework.context.aware.EnvironmentAware
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.util.BeanUtils
import com.wanna.framework.core.util.ClassUtils

/**
 * 这是一个解析器策略的工具类，它可以完成Bean的实例化，并回调相关的Aware接口(执行Aware接口，目前支持Environment/ClassLoader/BeanFactory的注入)
 */
@Suppress("UNCHECKED_CAST")
object ParserStrategyUtils {
    /**
     * 实例化BeanInstance，并完成Aware接口的回调
     */
    fun <T> instanceClass(
        clazz: Class<*>, environment: Environment, registry: BeanDefinitionRegistry
    ): T {
        // 获取classLoader
        val classLoader =
            if (registry is ConfigurableListableBeanFactory) registry.getBeanClassLoader() else ClassUtils.getDefaultClassLoader()
        // 使用BeanUtils去实例化对象
        val instance = createInstance(clazz, environment, registry, classLoader)
        // 执行Aware接口中的方法
        invokeAwareMethods(instance, environment, registry, classLoader)
        return instance as T
    }

    /**
     * 创建BeanInstance
     */
    private fun createInstance(
        clazz: Class<*>, environment: Environment, registry: BeanDefinitionRegistry, classLoader: ClassLoader?
    ): Any {
        return BeanUtils.instantiateClass(clazz)
    }

    /**
     * 执行Aware接口，目前支持Environment/ClassLoader/BeanFactory的注入
     */
    private fun invokeAwareMethods(
        bean: Any, environment: Environment, registry: BeanDefinitionRegistry, classLoader: ClassLoader
    ) {
        if (bean is EnvironmentAware) {
            bean.setEnvironment(environment)
        }
        if (bean is BeanClassLoaderAware) {
            bean.setBeanClassLoader(classLoader)
        }
        if (bean is BeanFactoryAware && registry is BeanFactory) {
            bean.setBeanFactory(registry)
        }
    }
}