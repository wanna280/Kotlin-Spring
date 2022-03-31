package com.wanna.framework.context.processor.factory.internal

import com.wanna.framework.beans.annotations.PriorityOrdered
import com.wanna.framework.context.BeanDefinitionRegistry
import com.wanna.framework.context.BeanFactory
import com.wanna.framework.context.annotations.AnnotationBeanNameGenerator
import com.wanna.framework.context.annotations.FullyQualifiedAnnotationBeanNameGenerator
import com.wanna.framework.context.aware.BeanClassLoaderAware
import com.wanna.framework.context.aware.EnvironmentAware
import com.wanna.framework.context.processor.factory.BeanDefinitionRegistryPostProcessor
import com.wanna.framework.context.util.ConfigurationClassBeanDefinitionReader
import com.wanna.framework.context.util.ConfigurationClassParser
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.environment.StandardEnvironment

/**
 * 这是一个配置类处理器，用来扫描Spring当中的配置类，包括Configuration/Component/Bean等注解的处理
 */
open class ConfigurationClassPostProcessor : BeanDefinitionRegistryPostProcessor, PriorityOrdered, EnvironmentAware,
    BeanClassLoaderAware {

    // order
    private var order: Int = 0

    // classLoader
    private var classLoader: ClassLoader? = null

    // 环境对象
    private var environment: Environment? = null

    // 配置类的解析器
    private var parser: ConfigurationClassParser? = null

    // 配置类的reader
    private var reader: ConfigurationClassBeanDefinitionReader? = null

    // componentScan的beanNameGenerator，默认使用simpleName作为生成方式
    private val componentScanBeanNameGenerator = AnnotationBeanNameGenerator.INSTANCE

    // import的Bean的beanNameGenerator，默认使用全限定名作为生成方式
    private val importBeanBeanNameGenerator = FullyQualifiedAnnotationBeanNameGenerator.INSTANCE

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        parser = ConfigurationClassParser(
            registry,
            StandardEnvironment(),
            ClassLoader.getSystemClassLoader(),
            componentScanBeanNameGenerator
        )
        reader = ConfigurationClassBeanDefinitionReader(registry, importBeanBeanNameGenerator)

        // 使用配置类解析器去进行解析配置类
        parser!!.parse()

        // 获取解析器解析到的所有配置类
        val configurationClasses = parser!!.getConfigurationClasses()

        // 加载BeanDefinition
        // 1.如果它是被@Import导入进来的，那么会在这里完成BeanDefinition的注册
        // 2.如果一个配置类有BeanMethod，那么会在这里完成注册
        // 3.如果一个配置类有ImportBeanDefinitionRegistrar，那么会在这里完成导入
        reader!!.loadBeanDefinitions(configurationClasses)
    }

    override fun postProcessBeanFactory(beanFactory: BeanFactory) {
        TODO("Not yet implemented")
    }

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    override fun getOrder(): Int {
        return order
    }

    override fun setBeanClassLoader(classLoader: ClassLoader) {
        this.classLoader = classLoader
    }

    fun getBeanClassLoader(): ClassLoader {
        return classLoader!!
    }

    open fun getEnvironment(): Environment {
        return this.environment!!
    }

    open fun setOrder(order: Int) {
        this.order = order
    }
}