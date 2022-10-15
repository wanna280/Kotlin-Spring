package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.definition.AnnotatedGenericBeanDefinition
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.environment.EnvironmentCapable
import com.wanna.framework.core.environment.StandardEnvironment
import com.wanna.framework.util.AnnotationConfigUtils

/**
 * 这是一个注解的BeanDefinitionReader，负责给定clazz，将其封装一个AnnotatedGenericBeanDefinition并注册到容器当中
 */
open class AnnotatedBeanDefinitionReader(private val registry: BeanDefinitionRegistry) {

    // 默认的beanNameGenerator为注解版本的的BeanNameGenerator，可以进行自定义
    private var beanNameGenerator: BeanNameGenerator? = AnnotationBeanNameGenerator.INSTANCE

    // 从registry当中获取到Environment，如果没有，那么就创建一个默认的Environment对象
    private var environment: Environment = getOrDefaultEnvironment(registry)

    private var scopeMetadataResolver: ScopeMetadataResolver = AnnotationScopeMetadataResolver()

    init {
        // 注册AnnotationConfig相关的PostProcessor
        AnnotationConfigUtils.registerAnnotationConfigProcessors(registry)
    }

    open fun setBeanNameGenerator(beanNameGenerator: BeanNameGenerator?) {
        this.beanNameGenerator = beanNameGenerator ?: AnnotationBeanNameGenerator.INSTANCE
    }

    open fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    /**
     * 注册一个Bean到容器中，将给定的clazz包装成为BeanDefinition注册到容器当中，beanName采用默认的BeanNameGenerator进行生成
     * @param clazz 要进行注册的clazz
     */
    protected open fun registerBean(clazz: Class<*>) {
        registerBean(clazz, null)
    }

    /**
     * 注册很多个配置类到容器当中
     */
    open fun registerBean(vararg componentClasses: Class<*>) {
        componentClasses.forEach(this::registerBean)
    }

    /**
     * 注册一个Bean到容器中，将给定的clazz包装成为BeanDefinition注册到容器当中
     * @param name 要指定的beanName(如果为空时，将会使用BeanNameGenerator进行生成)
     * @param clazz 要注册的类Class
     */
    open fun registerBean(clazz: Class<*>, name: String?) {
        // 创建一个AnnotatedGenericBeanDefinition
        val beanDefinition = AnnotatedGenericBeanDefinition(clazz)

        val scopeMetadata = scopeMetadataResolver.resolveScopeMetadata(beanDefinition)
        beanDefinition.setScope(scopeMetadata.scopeName)


        // 处理@Lazy/@Role/@DependsOn/@Primary注解
        AnnotationConfigUtils.processCommonDefinitionAnnotations(beanDefinition)
        // 生成BeanDefinition，注册到容器当中，如果给定了beanName，采用给定的beanName，如果没有给定，那么使用BeanNameGenerator去进行生成
        val beanName = name ?: beanNameGenerator!!.generateBeanName(beanDefinition, registry)
        registry.registerBeanDefinition(beanName, beanDefinition)
    }

    /**
     * 获取或者是创建一个默认的Environment，
     * (1)如果Registry可以获取到Environment，那么直接从Registry当中去获取到Environment对象;
     * (2)如果获取不到，那么就创建一个默认的Environment
     */
    protected fun getOrDefaultEnvironment(registry: BeanDefinitionRegistry): Environment {
        if (registry is EnvironmentCapable) {
            return registry.getEnvironment()
        }
        return StandardEnvironment()  // create default
    }
}