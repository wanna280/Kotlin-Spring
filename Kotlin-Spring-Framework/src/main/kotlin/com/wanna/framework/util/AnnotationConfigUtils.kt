package com.wanna.framework.util

import com.wanna.framework.beans.factory.support.BeanDefinitionHolder
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.context.BeanDefinitionRegistry
import com.wanna.framework.context.DefaultListableBeanFactory
import com.wanna.framework.context.GenericApplicationContext
import com.wanna.framework.context.event.DefaultEventListenerFactory
import com.wanna.framework.context.processor.beans.internal.AutowiredAnnotationPostProcessor
import com.wanna.framework.context.processor.beans.internal.CommonAnnotationPostProcessor
import com.wanna.framework.context.processor.factory.internal.ConfigurationClassPostProcessor
import com.wanna.framework.context.processor.factory.internal.EventListenerMethodProcessor
import com.wanna.framework.core.AnnotationOrderComparator

class AnnotationConfigUtils {
    companion object {

        const val CONFIGURATION_BEAN_NAME_GENERATOR = "beanNameGenerator";

        const val CONFIGURATION_ANOOTATION_PROCESSOR_BEAN_NAME = "internalConfigurationAnnotationProcessor"

        const val AUTOWIRED_ANNOTATION_PROCESSOR_BEAN_NAME = "internalAutowiredAnnotationProcessor"

        const val COMMON_ANNOTATION_PROCESSOR_BEAN_NAME = "internalCommonAnnotationProcessor"

        const val EVENT_LISTENER_PROCESSOR_BEAN_NAME = "internalEventListenerProcessor"

        const val EVENT_LISTENER_FACTORY_BEAN_NAME = "internalEventListenerFactory"

        /**
         * 将BeanDefinitionRegistry转为DefaultListableBeanFactory
         */
        private fun unwrapDefaultListableBeanFactory(registry: BeanDefinitionRegistry): DefaultListableBeanFactory? {
            if (registry is DefaultListableBeanFactory) {
                return registry
            } else if (registry is GenericApplicationContext) {
                return registry.getBeanFactory()
            } else {
                return null
            }
        }

        /**
         * 注册AnnotationConfig相关的Processor
         */
        fun registerAnnotationConfigProcessors(registry: BeanDefinitionRegistry): MutableSet<BeanDefinitionHolder> {
            return registerAnnotationConfigProcessors(registry, null)
        }

        /**
         * 注册AnnotationConfig相关的Processor
         */
        @JvmStatic
        fun registerAnnotationConfigProcessors(
            registry: BeanDefinitionRegistry,
            source: Any?
        ): MutableSet<BeanDefinitionHolder> {
            val beanFactory = unwrapDefaultListableBeanFactory(registry)
            if (beanFactory != null) {
                // 如果容器中的依赖比较器不是支持注解版的依赖比较器，那么就采用注解版的依赖比较器，去支持注解版的Order的比较
                if (!(beanFactory.getDependencyComparator() is AnnotationOrderComparator)) {
                    beanFactory.setDependencyComparator(AnnotationOrderComparator.INSTANCE)
                }
            }

            // 完成注册的beanDefinition列表
            val beanDefs = HashSet<BeanDefinitionHolder>()

            // 1.注册ConfigurationClassPostProcessor，处理注解版往容器中注册Bean的方式
            if (!registry.containsBeanDefinition(CONFIGURATION_ANOOTATION_PROCESSOR_BEAN_NAME)) {
                val rootBeanDefinition = RootBeanDefinition(ConfigurationClassPostProcessor::class.java)
                rootBeanDefinition.setSource(source)
                beanDefs += registerProcessor(
                    CONFIGURATION_ANOOTATION_PROCESSOR_BEAN_NAME,
                    rootBeanDefinition,
                    registry
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
         * 注册一个BeanDefinition到容器(BeanDefinitionRestryPostProcessor)中，并返回一个BeanDefinitionHolder
         */
        private fun registerProcessor(
            beanName: String,
            beanDefinition: RootBeanDefinition,
            registry: BeanDefinitionRegistry
        ): BeanDefinitionHolder {
            beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE)  // 设置role为基础设施Bean
            registry.registerBeanDefinition(beanName, beanDefinition)

            return BeanDefinitionHolder(beanDefinition, beanName)
        }
    }
}