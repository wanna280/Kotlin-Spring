package com.wanna.framework.context.processor.factory.internal

import com.wanna.framework.context.BeanDefinitionRegistry
import com.wanna.framework.context.BeanFactory
import com.wanna.framework.context.processor.factory.BeanDefinitionRegistryPostProcessor
import com.wanna.framework.context.util.ConfigurationClassBeanDefinitionReader
import com.wanna.framework.context.util.ConfigurationClassParser
import com.wanna.framework.core.environment.StandardEnvironment

/**
 * 这是一个配置类处理器，用来扫描Spring当中的配置类，包括Configuration/Component/Bean等注解的处理
 */
class ConfigurationClassPostProcessor : BeanDefinitionRegistryPostProcessor {

    // 配置类的解析器
    var parser: ConfigurationClassParser? = null

    // 配置类的reader
    var reader: ConfigurationClassBeanDefinitionReader? = null

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        parser = ConfigurationClassParser(registry, StandardEnvironment(), ClassLoader.getSystemClassLoader())
        reader = ConfigurationClassBeanDefinitionReader()

        // 使用配置类解析器去进行解析配置类
        parser!!.parse()

        // 获取解析器解析到的所有配置类
        val configurationClasses = parser!!.getConfigurationClasses()

        reader!!.loadBeanDefinitions(configurationClasses)
    }

    override fun postProcessBeanFactory(beanFactory: BeanFactory) {
        TODO("Not yet implemented")
    }
}