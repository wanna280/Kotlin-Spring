package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.BeanNameGenerator
import com.wanna.framework.beans.factory.support.definition.AnnotatedGenericBeanDefinition
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.environment.EnvironmentCapable
import com.wanna.framework.core.environment.StandardEnvironment

/**
 * 这是一个注解的BeanDefinitionReader, 负责给定clazz作为一个Spring的配置类, 
 * 将其封装一个[AnnotatedGenericBeanDefinition]并注册到给定的[BeanDefinitionRegistry]当中
 *
 * @param registry BeanDefinitionRegistry(需要将BeanDefinition去注册的地方)
 * @see BeanDefinitionRegistry.registerBeanDefinition
 */
open class AnnotatedBeanDefinitionReader(private val registry: BeanDefinitionRegistry) {

    /**
     * 默认的beanNameGenerator为注解版本的的BeanNameGenerator, 可以进行自定义
     */
    private var beanNameGenerator: BeanNameGenerator? = AnnotationBeanNameGenerator.INSTANCE

    /**
     * 从registry当中获取到Environment, 如果没有, 那么就先去创建出来一个默认的Environment对象
     */
    private var environment: Environment = getOrDefaultEnvironment(registry)

    /**
     * ScopeMetadataResolver,提供对于`@Scope`注解的解析
     */
    private var scopeMetadataResolver: ScopeMetadataResolver = AnnotationScopeMetadataResolver()

    init {
        // 注册AnnotationConfig相关的PostProcessor
        AnnotationConfigUtils.registerAnnotationConfigProcessors(registry)
    }

    /**
     * 自定义BeanNameGenerator
     *
     * @param beanNameGenerator BeanName的生成器(为null代表使用实例)
     */
    open fun setBeanNameGenerator(beanNameGenerator: BeanNameGenerator?) {
        this.beanNameGenerator = beanNameGenerator ?: AnnotationBeanNameGenerator.INSTANCE
    }

    /**
     * 自定义Environment
     *
     * @param environment Environment
     */
    open fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    /**
     * 注册一个Bean到容器中, 将给定的clazz包装成为BeanDefinition注册到容器当中; 
     * beanName将会采用默认的BeanNameGenerator进行生成
     *
     * @param clazz 要进行注册的clazz
     */
    protected open fun registerBean(clazz: Class<*>) {
        registerBean(clazz, null)
    }

    /**
     * 注册很多个配置类到容器当中
     *
     * @param componentClasses 需要去进行匹配注册的配置类
     */
    open fun registerBean(vararg componentClasses: Class<*>) {
        componentClasses.forEach(this::registerBean)
    }

    /**
     * 注册一个Bean到容器中, 将给定的clazz包装成为BeanDefinition注册到容器当中
     *
     * @param name 要指定的beanName(如果为空时, 将会使用BeanNameGenerator进行生成)
     * @param clazz 要注册的类Class
     */
    open fun registerBean(clazz: Class<*>, name: String?) {
        // 创建一个AnnotatedGenericBeanDefinition
        val beanDefinition = AnnotatedGenericBeanDefinition(clazz)

        val scopeMetadata = scopeMetadataResolver.resolveScopeMetadata(beanDefinition)
        beanDefinition.setScope(scopeMetadata.scopeName)


        // 处理@Lazy/@Role/@DependsOn/@Primary注解
        AnnotationConfigUtils.processCommonDefinitionAnnotations(beanDefinition)
        // 生成BeanDefinition, 注册到容器当中, 如果给定了beanName, 采用给定的beanName, 如果没有给定, 那么使用BeanNameGenerator去进行生成
        val beanName = name ?: beanNameGenerator!!.generateBeanName(beanDefinition, registry)
        registry.registerBeanDefinition(beanName, beanDefinition)
    }

    /**
     * 获取或者是创建一个默认的Environment, 
     * (1)如果Registry可以获取到Environment, 那么直接从Registry当中去获取到Environment对象;
     * (2)如果获取不到, 那么就创建一个默认的Environment
     *
     * @param registry BeanDefinitionRegistry
     * @return Environment
     */
    protected fun getOrDefaultEnvironment(registry: BeanDefinitionRegistry): Environment {
        if (registry is EnvironmentCapable) {
            return registry.getEnvironment()
        }
        return StandardEnvironment()  // create default
    }
}