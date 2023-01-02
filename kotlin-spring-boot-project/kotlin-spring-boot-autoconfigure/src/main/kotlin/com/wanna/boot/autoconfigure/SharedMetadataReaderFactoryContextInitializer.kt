package com.wanna.boot.autoconfigure

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.beans.factory.support.definition.GenericBeanDefinition
import com.wanna.framework.context.ApplicationContextInitializer
import com.wanna.framework.context.ConfigurableApplicationContext
import com.wanna.framework.context.processor.factory.BeanDefinitionRegistryPostProcessor
import com.wanna.framework.core.Ordered
import com.wanna.framework.core.PriorityOrdered
import com.wanna.framework.core.type.classreading.CachingMetadataReaderFactory

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

    class CachingMetadataReaderFactoryPostProcessor(private val applicationContext: ConfigurableApplicationContext) :
        BeanDefinitionRegistryPostProcessor, PriorityOrdered {
        override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {
            register(registry)
            // TODO
        }

        private fun register(registry: BeanDefinitionRegistry) {
            val beanDef = GenericBeanDefinition()
            beanDef.setBeanClass(CachingMetadataReaderFactory::class.java)
            beanDef.setInstanceSupplier { CachingMetadataReaderFactory(applicationContext) }
            registry.registerBeanDefinition(BEAN_NAME, beanDef)
        }

        override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
            // nothing to do
        }

        /**
         * 给它最高优先级, 它必须出现在ConfigurationClassPostProcessor的之前
         */
        override fun getOrder(): Int = Ordered.ORDER_HIGHEST
    }
}