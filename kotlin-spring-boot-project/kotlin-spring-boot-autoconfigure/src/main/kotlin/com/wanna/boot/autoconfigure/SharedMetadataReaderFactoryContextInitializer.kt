package com.wanna.boot.autoconfigure

import com.wanna.framework.beans.MutablePropertyValues
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.beans.factory.config.RuntimeBeanReference
import com.wanna.framework.beans.factory.support.definition.AbstractBeanDefinition
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.GenericBeanDefinition
import com.wanna.framework.context.ApplicationContextInitializer
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.beans.factory.exception.NoSuchBeanDefinitionException
import com.wanna.framework.context.processor.factory.BeanDefinitionRegistryPostProcessor
import com.wanna.framework.context.processor.factory.internal.ConfigurationClassPostProcessor
import com.wanna.framework.core.Ordered
import com.wanna.framework.core.PriorityOrdered
import com.wanna.framework.core.type.classreading.CachingMetadataReaderFactory
import com.wanna.framework.core.type.classreading.MetadataReaderFactory
import com.wanna.framework.context.annotation.AnnotationConfigUtils
import java.util.function.Supplier

/**
 * 共享的MetadataReaderFactory的ApplicationContext初始化器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/2
 */
class SharedMetadataReaderFactoryContextInitializer : ApplicationContextInitializer<ConfigurableApplicationContext>,
    Ordered {

    companion object {

        /**
         * 要去进行导入的共享的MetadataReaderFactory的beanName
         */
        const val BEAN_NAME = "com.wanna.boot.autoconfigure.internalMetadataReaderFactory"
    }


    /**
     * 初始化ApplicationContext, 给ApplicationContext当中添加一个导入MetadataReaderFactory的BeanPostProcessor
     *
     * @param applicationContext ApplicationContext
     */
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        applicationContext.addBeanFactoryPostProcessor(CachingMetadataReaderFactoryPostProcessor(applicationContext))
    }

    override fun getOrder(): Int = 0

    /**
     * CachingMetadataReaderFactory的注册的BeanDefinitionRegistryPostProcessor
     *
     * @param applicationContext ApplicationContext
     */
    class CachingMetadataReaderFactoryPostProcessor(private val applicationContext: ConfigurableApplicationContext) :
        BeanDefinitionRegistryPostProcessor, PriorityOrdered {
        override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
            // 给BeanFactory当中注册一个MetadataReaderFactory对象
            register(registry)

            // 为ConfigurationClassPostProcessor去添加MetadataReaderFactory字段的设置
            configureConfigurationClassPostProcessor(registry)
        }

        /**
         * 给BeanDefinitionRegistry当中去注册一个MetadataReaderFactory的Bean
         *
         * @param registry BeanDefinitionRegistry
         */
        private fun register(registry: BeanDefinitionRegistry) {
            val beanDef = GenericBeanDefinition()
            beanDef.setBeanClass(CachingMetadataReaderFactory::class.java)
            beanDef.setInstanceSupplier { CachingMetadataReaderFactory(applicationContext) }
            registry.registerBeanDefinition(BEAN_NAME, beanDef)
        }

        /**
         * 对于已经注册的ConfigurationClassPostProcessor去进行处理
         *
         * @param registry BeanDefinitionRegistry
         */
        private fun configureConfigurationClassPostProcessor(registry: BeanDefinitionRegistry) {
            try {
                val bd = registry.getBeanDefinition(AnnotationConfigUtils.CONFIGURATION_ANNOTATION_PROCESSOR_BEAN_NAME)
                configureConfigurationClassPostProcessor(bd)
            } catch (ex: NoSuchBeanDefinitionException) {
                // ignore if not exists
            }
        }

        /**
         * 对于ConfigurationClassPostProcessor的BeanDefinition去进行自定义, 让它可以实现MetadataReaderFactory的注入
         *
         * @param beanDefinition ConfigurationClassPostProcessor的BeanDefinition
         */
        private fun configureConfigurationClassPostProcessor(beanDefinition: BeanDefinition) {
            // 如果是AbstractBeanDefinition的话,  如果是给定的InstanceSupplier的话, 那么对InstanceSupplier去进行自定义
            if (beanDefinition is AbstractBeanDefinition) {
                configureConfigurationClassPostProcessor(beanDefinition)
                return
            }

            // 如果不是AbstractBeanDefinition的话, 那么只是简单自定义PropertyValues...
            configureConfigurationClassPostProcessor(beanDefinition.getPropertyValues())
        }

        /**
         * 如果是AbstractBeanDefinition并且有InstanceSupplier的话, 那么对InstanceSupplier去进行自定义
         *
         * @param beanDefinition ConfigurationClassPostProcessor的BeanDefinition
         */
        private fun configureConfigurationClassPostProcessor(beanDefinition: AbstractBeanDefinition) {
            val instanceSupplier = beanDefinition.getInstanceSupplier()
            if (instanceSupplier != null) {
                beanDefinition.setInstanceSupplier(
                    ConfigurationClassPostProcessorCustomizingSupplier(this.applicationContext, instanceSupplier)
                )
                return
            }
            configureConfigurationClassPostProcessor(beanDefinition.getPropertyValues())
        }

        /**
         * 对BeanDefinition当中的PropertyValues去进行处理, 添加一个MetadataReaderFactory字段
         *
         * @param propertyValues ConfigurationClassPostProcessor的BeanDefinition当中的PropertyValues
         */
        private fun configureConfigurationClassPostProcessor(propertyValues: MutablePropertyValues) {
            propertyValues.addPropertyValue("metadataReaderFactory", RuntimeBeanReference(BEAN_NAME))
        }

        override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
            // nothing to do
        }

        /**
         * 给它最高优先级, 它必须出现在ConfigurationClassPostProcessor的之前
         */
        override fun getOrder(): Int = Ordered.ORDER_HIGHEST
    }

    /**
     * 针对ConfigurationClassPostProcessor的InstanceSupplier, 去进行自定义, 进行MetadataReader的设置
     *
     * @param applicationContext ApplicationContext
     * @param instanceSupplier ConfigurationClassPostProcessor的BeanDefinition原本的InstanceSupplier
     */
    private class ConfigurationClassPostProcessorCustomizingSupplier(
        private val applicationContext: ConfigurableApplicationContext, private val instanceSupplier: Supplier<*>
    ) : Supplier<Any> {
        override fun get(): Any {
            val instance = instanceSupplier.get()

            // 对它的MetadataReaderFactory去进行自定义...
            if (instance is ConfigurationClassPostProcessor) {
                configureConfigurationClassPostProcessor(instance)
            }
            return instance
        }

        private fun configureConfigurationClassPostProcessor(configurationClassPostProcessor: ConfigurationClassPostProcessor) {
            val metadataReaderFactory = applicationContext.getBean(BEAN_NAME, MetadataReaderFactory::class.java)
            configurationClassPostProcessor.setMetadataReaderFactory(metadataReaderFactory)
        }
    }
}