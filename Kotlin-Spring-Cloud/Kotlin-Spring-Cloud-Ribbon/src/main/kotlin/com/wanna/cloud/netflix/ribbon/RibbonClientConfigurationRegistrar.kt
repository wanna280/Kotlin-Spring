package com.wanna.cloud.netflix.ribbon

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.definition.GenericBeanDefinition
import com.wanna.framework.context.annotation.AnnotationAttributesUtils
import com.wanna.framework.context.annotation.ImportBeanDefinitionRegistrar
import com.wanna.framework.core.type.AnnotationMetadata

/**
 * 负责处理@RibbonClient的ImportBeanDefinitionRegistrar，负责给容器中导入组件(导入Specification)
 */
@Suppress("UNCHECKED_CAST")
open class RibbonClientConfigurationRegistrar : ImportBeanDefinitionRegistrar {
    override fun registerBeanDefinitions(annotationMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        val clientsAttributes = annotationMetadata.getAnnotationAttributes(RibbonClients::class.java)
        if (clientsAttributes.isNotEmpty()) {
            // 1.遍历RibbonClients当中的所有RibbonClient注解，去进行处理
            val ribbonClients = clientsAttributes["value"] as Array<RibbonClient>
            ribbonClients.forEach {
                registerRibbonClient(
                    registry, AnnotationAttributesUtils.asAnnotationAttributes(it)!!
                )
            }

            // 将defaultConfiguration注册一下，需要加上前缀default.代表对所有的childContext(Service)生效...
            val defaultConfigurations = clientsAttributes["defaultConfiguration"] as Array<Class<*>>
            defaultConfigurations.forEach {
                registerClientConfiguration(registry, "default." + it.name, arrayOf(it))
            }

        }


        val attributes = annotationMetadata.getAnnotationAttributes(RibbonClient::class.java)
        if (attributes.isNotEmpty()) {
            registerRibbonClient(registry, attributes)
        }
    }

    private fun registerRibbonClient(registry: BeanDefinitionRegistry, attributes: Map<String, Any>) {
        registerClientConfiguration(registry, attributes["name"].toString(), attributes["configuration"] as Array<Class<*>>)
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