package com.wanna.boot.context.properties

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.context.annotation.ImportBeanDefinitionRegistrar
import com.wanna.framework.core.type.AnnotationMetadata

/**
 * 它是一个ConfigurationProperties的Registrar，负责往容器当中注册@ConfigurationProperties注解的处理器
 *
 * @see EnableConfigurationProperties
 * @see ConfigurationProperties
 */
open class EnableConfigurationPropertiesRegistrar : ImportBeanDefinitionRegistrar {
    override fun registerBeanDefinitions(annotationMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        // 1.往容器当中注册一些关于处理ConfigurationProperties的基础设施Bean
        registerInfrastructureBeans(registry)

        // 2.拿到@EnableConfigurationProperties注解value属性当中配置的Properties类列表，并注册到容器当中
        val registrar = ConfigurationPropertiesBeanRegistrar(registry)
        getTypes(annotationMetadata).forEach { registrar.register(it) }
    }

    /**
     * 从@EnableConfigurationProperties注解当中拿到配置的Properties类列表
     *
     * @see EnableConfigurationProperties.value
     */
    @Suppress("UNCHECKED_CAST")
    private fun getTypes(metadata: AnnotationMetadata): Array<Class<*>> {
        val attributes = metadata.getAnnotationAttributes(EnableConfigurationProperties::class.java)
        return attributes["value"] as Array<Class<*>>
    }

    companion object {
        /**
         * 给容器中注册一些基础设施Bean，主要就是一些处理@ConfigurationProperties的处理器
         *
         * @see ConfigurationPropertiesBindingPostProcessor
         */
        fun registerInfrastructureBeans(registry: BeanDefinitionRegistry) {
            // 注册ConfigurationPropertiesBinderPostProcessor，内部会注册ConfigurationPropertiesBinder组件
            ConfigurationPropertiesBindingPostProcessor.register(registry)
        }
    }
}