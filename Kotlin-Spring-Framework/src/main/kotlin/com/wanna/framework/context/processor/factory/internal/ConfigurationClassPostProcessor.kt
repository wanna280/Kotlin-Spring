package com.wanna.framework.context.processor.factory.internal

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.beans.factory.support.BeanDefinitionHolder
import com.wanna.framework.beans.util.ConfigurationClassUtils
import com.wanna.framework.context.ApplicationStartupAware
import com.wanna.framework.context.SingletonBeanRegistry
import com.wanna.framework.context.annotation.AnnotationBeanNameGenerator
import com.wanna.framework.context.annotation.BeanNameGenerator
import com.wanna.framework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator
import com.wanna.framework.context.aware.BeanClassLoaderAware
import com.wanna.framework.context.aware.EnvironmentAware
import com.wanna.framework.context.processor.factory.BeanDefinitionRegistryPostProcessor
import com.wanna.framework.context.annotation.ConfigurationClassBeanDefinitionReader
import com.wanna.framework.context.annotation.ConfigurationClassParser
import com.wanna.framework.core.PriorityOrdered
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.environment.StandardEnvironment
import com.wanna.framework.core.metrics.ApplicationStartup
import com.wanna.framework.core.util.AnnotationConfigUtils

/**
 * 这是一个配置类处理器，用来扫描Spring当中的配置类，包括Configuration/Component/Bean等注解的处理
 */
open class ConfigurationClassPostProcessor : BeanDefinitionRegistryPostProcessor, PriorityOrdered, EnvironmentAware,
    BeanClassLoaderAware, ApplicationStartupAware {

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
    private var componentScanBeanNameGenerator: BeanNameGenerator = AnnotationBeanNameGenerator.INSTANCE

    // import的Bean的beanNameGenerator，默认使用全限定名作为生成方式
    private var importBeanBeanNameGenerator: BeanNameGenerator = FullyQualifiedAnnotationBeanNameGenerator.INSTANCE

    // 是否设置了局部的BeanGenerator，如果设置了，将会采用默认的BeanNameGenerator
    private var localBeanNameGeneratorSet = false

    // ApplicationStartup
    private var applicationStartup: ApplicationStartup? = null

    /**
     * 设置局部的BeanNameGenerator，设置之后整个扫描过程都会采用给定的BeanNameGenerator作为beanName的生成器
     */
    open fun setBeanNameGenerator(generator: BeanNameGenerator) {
        localBeanNameGeneratorSet = true
        this.importBeanBeanNameGenerator = generator;
        this.componentScanBeanNameGenerator = generator;
    }

    override fun setApplicationStartup(applicationStartup: ApplicationStartup) {
        this.applicationStartup = applicationStartup
    }

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
        // 候选的BeanDefinition列表，包含了beanDefinition和beanName
        val candidates = ArrayList<BeanDefinitionHolder>()
        val beanDefinitionNames = registry.getBeanDefinitionNames()
        beanDefinitionNames.forEach { beanName ->
            val beanDefinition = registry.getBeanDefinition(beanName)
            // 如果已经包含了配置类的属性，说明该配置类已经处理过了，不需要去进行继续处理了
            if (beanDefinition!!.getAttribute(ConfigurationClassUtils.CONFIGURATION_CLASS_ATTRIBUTE) != null) {

                // 检查配置类是否是配置类，如果是配置类，那么说明它应该作为配置类去进行处理，加入候选列表当
            } else if (ConfigurationClassUtils.checkConfigurationClassCandidate(beanDefinition)) {
                candidates.add(BeanDefinitionHolder(beanDefinition, beanName))
            }
        }

        // 如果没有设置局部的BeanNameGenerator，尝试从SingletonBeanRegistry当中去获取到BeanNameGenerator
        // 如果找到了，那么就去替换默认的BeanNameGenerator
        if (registry is SingletonBeanRegistry && !localBeanNameGeneratorSet) {
            val beanNameGenerator = registry.getSingleton(AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR)
            if (beanNameGenerator != null) {
                this.importBeanBeanNameGenerator = beanNameGenerator as BeanNameGenerator;
                this.componentScanBeanNameGenerator = beanNameGenerator as BeanNameGenerator;
            }
        }

        parser = ConfigurationClassParser(
            registry,
            StandardEnvironment(),
            ClassLoader.getSystemClassLoader(),
            componentScanBeanNameGenerator
        )
        reader = ConfigurationClassBeanDefinitionReader(registry, importBeanBeanNameGenerator, environment!!)

        val parseConfig = this.applicationStartup!!.start("spring.context.config-classes.parse")  // start parseConfig

        // 使用配置类解析器去进行解析配置类
        parser!!.parse(candidates)

        // 获取解析器解析到的所有配置类
        val configurationClasses = parser!!.getConfigurationClasses()

        // 加载BeanDefinition
        // 1.如果它是被@Import导入进来的，那么会在这里完成BeanDefinition的注册
        // 2.如果一个配置类有BeanMethod，那么会在这里完成注册
        // 3.如果一个配置类有ImportBeanDefinitionRegistrar，那么会在这里完成导入
        reader!!.loadBeanDefinitions(configurationClasses)

        parseConfig.tag("classCount", "${configurationClasses.size}").end()  // tag and end
    }

    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {

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