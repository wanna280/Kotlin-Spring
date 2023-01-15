package com.wanna.cloud.netflix.ribbon

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.definition.GenericBeanDefinition
import com.wanna.framework.context.annotation.ImportBeanDefinitionRegistrar
import com.wanna.framework.core.annotation.*
import com.wanna.framework.core.type.AnnotationMetadata

/**
 * 负责处理@RibbonClient的ImportBeanDefinitionRegistrar, 负责给容器中导入组件(导入Specification)
 */
@Suppress("UNCHECKED_CAST")
open class RibbonClientConfigurationRegistrar : ImportBeanDefinitionRegistrar {
    override fun registerBeanDefinitions(annotationMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        val clientsAttributes = annotationMetadata.getAnnotations().get(RibbonClients::class.java)
        if (clientsAttributes.present) {
            // 遍历RibbonClients当中的所有RibbonClient注解, 去进行处理
            val ribbonClients = clientsAttributes.getAnnotationArray("value", Array<RibbonClient>::class.java)
            ribbonClients.forEach {
                val annotation =
                    MergedAnnotations.from(null, arrayOf(it), RepeatableContainers.none(), AnnotationFilter.PLAIN)
                        .get(RibbonClient::class.java)
                registerRibbonClient(registry, annotation)
            }

            // 将defaultConfiguration注册一下, 需要加上前缀"default."代表对所有的childContext(Service)生效...
            val defaultConfigurations = clientsAttributes.getClassArray("defaultConfiguration")
            defaultConfigurations.forEach {
                registerClientConfiguration(registry, "default." + it.name, arrayOf(it))
            }

        }

        // 处理单个@RibbonClient
        val attributes = annotationMetadata.getAnnotations().get(RibbonClient::class.java)
        if (attributes.present) {
            registerRibbonClient(registry, attributes)
        }
    }

    /**
     * 处理一个@RibbonClient, 将注解当中的相关信息, 注册成为Specification
     *
     * @param registry registry
     * @param attributes @RibbonClient当中的配置信息
     */
    private fun registerRibbonClient(registry: BeanDefinitionRegistry, attributes: MergedAnnotation<*>) {
        registerClientConfiguration(
            registry, attributes.getString("name"), attributes.getClassArray("configuration")
        )
    }

    /**
     * 注册Specification...
     */
    private fun registerClientConfiguration(
        registry: BeanDefinitionRegistry, name: String, configuration: Array<Class<*>>
    ) {
        val genericBeanDefinition = GenericBeanDefinition(RibbonClientSpecification::class.java)
        genericBeanDefinition.setInstanceSupplier {
            RibbonClientSpecification(name, configuration)
        }
        registry.registerBeanDefinition(name, genericBeanDefinition)
    }
}