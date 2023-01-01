package com.wanna.framework.util

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.BeanDefinitionHolder
import com.wanna.framework.beans.factory.support.ContextAnnotationAutowireCandidateResolver
import com.wanna.framework.beans.factory.support.DefaultListableBeanFactory
import com.wanna.framework.beans.factory.support.definition.AnnotatedBeanDefinition
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.context.annotation.DependsOn
import com.wanna.framework.context.annotation.Lazy
import com.wanna.framework.context.annotation.Primary
import com.wanna.framework.context.annotation.Role
import com.wanna.framework.context.event.DefaultEventListenerFactory
import com.wanna.framework.context.processor.beans.internal.AutowiredAnnotationPostProcessor
import com.wanna.framework.context.processor.beans.internal.CommonAnnotationPostProcessor
import com.wanna.framework.context.processor.factory.internal.ConfigurationClassPostProcessor
import com.wanna.framework.context.event.EventListenerMethodProcessor
import com.wanna.framework.context.support.GenericApplicationContext
import com.wanna.framework.core.annotation.MergedAnnotation
import com.wanna.framework.core.comparator.AnnotationAwareOrderComparator
import com.wanna.framework.core.type.AnnotatedTypeMetadata

object AnnotationConfigUtils {

    // BeanNameGenerator的beanName
    const val CONFIGURATION_BEAN_NAME_GENERATOR = "com.wanna.framework.context.annotation.internBeanNameGenerator"

    const val CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME =
        "com.wanna.framework.context.annotation.internalConfigurationAnnotationProcessor"

    const val AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME =
        "com.wanna.framework.context.annotation.internalAutowiredAnnotationProcessor"

    const val COMMON_ANNOTATION_PROCESSOR_BEAN_NAME =
        "com.wanna.framework.context.annotation.internalCommonAnnotationProcessor"

    const val EVENT_LISTENER_PROCESSOR_BEAN_NAME =
        "com.wanna.framework.context.annotation.internalEventListenerProcessor"

    const val EVENT_LISTENER_FACTORY_BEAN_NAME = "com.wanna.framework.context.annotation.internalEventListenerFactory"

    /**
     * 将BeanDefinitionRegistry转为DefaultListableBeanFactory
     */
    @JvmStatic
    private fun unwrapDefaultListableBeanFactory(registry: BeanDefinitionRegistry): DefaultListableBeanFactory? {
        return when (registry) {
            is DefaultListableBeanFactory -> registry
            is GenericApplicationContext -> registry.getDefaultListableBeanFactory()
            else -> null
        }
    }

    /**
     * 处理通用的BeanDefinition注解，包括@Primary/@Lazy/@DependsOn/@Role等注解；
     * 需要使用的metadata信息从BeanDefinition当中获取即可
     *
     * @param abd BeanDefinition
     */
    @JvmStatic
    fun processCommonDefinitionAnnotations(abd: AnnotatedBeanDefinition) {
        processCommonDefinitionAnnotations(abd, abd.getMetadata())
    }

    /**
     * 处理通用的BeanDefinition注解
     *
     * @param metadata 注解信息的来源
     * @param abd 要进行设置属性的BeanDefinition
     */
    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun processCommonDefinitionAnnotations(abd: AnnotatedBeanDefinition, metadata: AnnotatedTypeMetadata) {

        // 如果标注了@Primary注解，将BeanDefinition的Primary设置为true
        val primary = metadata.isAnnotated(Primary::class.java.name)
        if (primary) {
            abd.setPrimary(true)
        }

        // 如果标注了@Lazy注解，将BeanDefinition的LazyInit设置为true
        val lazy = metadata.isAnnotated(Lazy::class.java.name)
        if (lazy) {
            abd.setLazyInit(true)
        }

        // 如果标注了Role注解，需要设置Bean的Role信息
        val role = metadata.isAnnotated(Role::class.java.name)
        if (role) {
            val bdRole = metadata.getAnnotations().get(Role::class.java).getInt(MergedAnnotation.VALUE)
            abd.setRole(bdRole)
        }

        // 如果标注了DependsOn注解的话
        val dependsOn = metadata.isAnnotated(DependsOn::class.java.name)
        if (dependsOn) {
            val bdDependsOn =
                metadata.getAnnotations().get(DependsOn::class.java).getStringArray(MergedAnnotation.VALUE)
            abd.setDependsOn(bdDependsOn)
        }
    }

    /**
     * 注册AnnotationConfig相关的Processor
     *
     * @param registry 需要注册注解配置的BeanDefinitionRegistry
     * @return 注册得到的BeanDefinition列表
     */
    @JvmStatic
    fun registerAnnotationConfigProcessors(registry: BeanDefinitionRegistry): MutableSet<BeanDefinitionHolder> {
        return registerAnnotationConfigProcessors(registry, null)
    }

    /**
     * 注册AnnotationConfig相关的Processor
     */
    @JvmStatic
    fun registerAnnotationConfigProcessors(
        registry: BeanDefinitionRegistry, source: Any?
    ): MutableSet<BeanDefinitionHolder> {
        val beanFactory = unwrapDefaultListableBeanFactory(registry)
        if (beanFactory != null) {
            // 如果容器中的依赖比较器不是支持注解版的依赖比较器，那么就采用注解版的依赖比较器，去支持注解版的Order的比较
            if (beanFactory.getDependencyComparator() !is AnnotationAwareOrderComparator) {
                beanFactory.setDependencyComparator(AnnotationAwareOrderComparator.INSTANCE)
            }

            // 设置AutowireCandidate的Resolver，主要用来完成自动注入的元素的匹配
            if (beanFactory.getAutowireCandidateResolver() !is ContextAnnotationAutowireCandidateResolver) {
                beanFactory.setAutowireCandidateResolver(ContextAnnotationAutowireCandidateResolver.INSTANCE)
            }
        }

        // 完成注册的beanDefinition列表
        val beanDefs = HashSet<BeanDefinitionHolder>()

        // 1.注册ConfigurationClassPostProcessor，处理注解版往容器中注册Bean的方式
        if (!registry.containsBeanDefinition(CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME)) {
            val rootBeanDefinition = RootBeanDefinition(ConfigurationClassPostProcessor::class.java)
            rootBeanDefinition.setSource(source)
            beanDefs += registerProcessor(
                CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME, rootBeanDefinition, registry
            )
        }

        // 2.注册@Autowired/@Inject注解的Processor
        if (!registry.containsBeanDefinition(AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME)) {
            val rootBeanDefinition = RootBeanDefinition(AutowiredAnnotationPostProcessor::class.java)
            rootBeanDefinition.setSource(source)
            beanDefs += registerProcessor(AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME, rootBeanDefinition, registry)
        }

        // 3.注册通用注解的Processor，包括@PostConstruct/@Resource等注解
        if (!registry.containsBeanDefinition(COMMON_ANNOTATION_PROCESSOR_BEAN_NAME)) {
            val rootBeanDefinition = RootBeanDefinition(CommonAnnotationPostProcessor::class.java)
            rootBeanDefinition.setSource(source)
            beanDefs += registerProcessor(COMMON_ANNOTATION_PROCESSOR_BEAN_NAME, rootBeanDefinition, registry)
        }

        // 4.注册EventListener的Processor
        if (!registry.containsBeanDefinition(EVENT_LISTENER_PROCESSOR_BEAN_NAME)) {
            val rootBeanDefinition = RootBeanDefinition(EventListenerMethodProcessor::class.java)
            rootBeanDefinition.setSource(source)
            beanDefs += registerProcessor(EVENT_LISTENER_PROCESSOR_BEAN_NAME, rootBeanDefinition, registry)
        }

        // 5.注册用来创建EventListener的Factory的Bean
        if (!registry.containsBeanDefinition(EVENT_LISTENER_FACTORY_BEAN_NAME)) {
            val rootBeanDefinition = RootBeanDefinition(DefaultEventListenerFactory::class.java)
            rootBeanDefinition.setSource(source)
            beanDefs += registerProcessor(EVENT_LISTENER_FACTORY_BEAN_NAME, rootBeanDefinition, registry)
        }

        return beanDefs
    }

    /**
     * 注册一个BeanDefinition到容器(BeanDefinitionRegistryPostProcessor)中，并返回一个BeanDefinitionHolder
     */
    @JvmStatic
    private fun registerProcessor(
        beanName: String, beanDefinition: RootBeanDefinition, registry: BeanDefinitionRegistry
    ): BeanDefinitionHolder {
        beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE)  // 设置role为基础设施Bean
        registry.registerBeanDefinition(beanName, beanDefinition)

        return BeanDefinitionHolder(beanDefinition, beanName)
    }
}