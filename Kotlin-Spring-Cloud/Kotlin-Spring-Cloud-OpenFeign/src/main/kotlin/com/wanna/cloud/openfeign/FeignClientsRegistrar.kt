package com.wanna.cloud.openfeign

import com.wanna.cloud.context.named.NamedContextFactory
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.config.ConfigurableBeanFactory
import com.wanna.framework.beans.factory.support.definition.AnnotatedBeanDefinition
import com.wanna.framework.beans.factory.support.definition.AnnotatedGenericBeanDefinition
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.GenericBeanDefinition
import com.wanna.framework.context.annotation.ClassPathBeanDefinitionScanner
import com.wanna.framework.context.annotation.ImportBeanDefinitionRegistrar
import com.wanna.framework.context.aware.EnvironmentAware
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.type.AnnotationMetadata
import com.wanna.framework.core.type.StandardAnnotationMetadata
import com.wanna.framework.core.type.filter.AnnotationTypeFilter
import com.wanna.framework.core.util.ClassUtils
import com.wanna.framework.core.util.StringUtils

/**
 * 用来处理@EnableFeignClients以及@FeignClient的ImportBeanDefinitionRegistrar；
 */
@Suppress("UNCHECKED_CAST")
open class FeignClientsRegistrar : ImportBeanDefinitionRegistrar, EnvironmentAware {

    private var environment: Environment? = null

    override fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    override fun registerBeanDefinitions(annotationMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        val attributes = annotationMetadata.getAnnotationAttributes(EnableFeignClients::class.java)
        if (attributes.isNotEmpty()) {
            // 注册默认的配置类列表...
            val configurations = attributes["defaultConfiguration"] as Array<Class<*>>
            registerClientConfiguration(
                registry, NamedContextFactory.DEFAULT_PREFIX + annotationMetadata.getClassName(), configurations
            )

            registerFeignClients(registry, attributes)
        }
    }

    private fun registerFeignClients(registry: BeanDefinitionRegistry, attributes: Map<String, Any>) {
        val basePackages = getBasePackages(attributes)
        val candidateComponents = LinkedHashSet<BeanDefinition>()
        val scanner = getScanner(registry)
        // 处理扫描到的组件的列表
        basePackages.forEach { candidateComponents += scanner.findCandidateComponents(it) }

        val clients = attributes["clients"] as Array<Class<*>>
        val clientsBeanDefinitions = clients.map { AnnotatedGenericBeanDefinition(it) }.toList()
        candidateComponents += clientsBeanDefinitions

        candidateComponents.forEach {
            if (it is AnnotatedBeanDefinition) {
                val metadata = it.getMetadata()
                val clientAttributes = metadata.getAnnotationAttributes(FeignClient::class.java)
                val clientName = getClientName(clientAttributes)
                // 注册ClientConfigurations
                registerClientConfiguration(registry, clientName, clientAttributes["configuration"] as Array<Class<*>>)

                // 注册FeignClient
                registerFeignClient(registry, metadata, clientAttributes)
            }
        }
    }

    private fun registerClientConfiguration(
        registry: BeanDefinitionRegistry, name: String, configurations: Array<Class<*>>
    ) {
        val beanDefinition = GenericBeanDefinition(FeignClientSpecification::class.java)
        beanDefinition.setInstanceSupplier { FeignClientSpecification(name, configurations) }
        registry.registerBeanDefinition(name + "." + FeignClientSpecification::class.java.name, beanDefinition)
    }

    private fun registerFeignClient(
        registry: BeanDefinitionRegistry, metadata: AnnotationMetadata, attributes: Map<String, Any>
    ) {
        val factoryBean = FeignClientFactoryBean()
        factoryBean.name = getClientName(attributes)
        if (metadata is StandardAnnotationMetadata) {
            factoryBean.type = metadata.clazz
        } else {
            factoryBean.type = ClassUtils.forName<Any>(metadata.getClassName())
        }
        factoryBean.contextId = getContextId(attributes)
        factoryBean.url = attributes["url"] as String
        factoryBean.path = attributes["path"] as String
        val beanFactory: ConfigurableBeanFactory? = if (registry is ConfigurableBeanFactory) registry else null
        if (beanFactory != null) {
            factoryBean.setBeanFactory(beanFactory)
        }
        val beanDefinition = GenericBeanDefinition(factoryBean.type)
        beanDefinition.setInstanceSupplier(factoryBean::getTarget)

        // 注册BeanDefinition到registry当中
        registry.registerBeanDefinition(factoryBean.name!!, beanDefinition)
    }

    private fun getClientName(attributes: Map<String, Any>): String {

        var name = attributes["contextId"] as String
        if (!StringUtils.hasText(name)) {
            name = attributes["value"] as String
        }
        if (!StringUtils.hasText(name)) {
            name = attributes["name"] as String
        }
        if (!StringUtils.hasText(name)) {
            throw IllegalStateException("@FeignClient必须配置value/contextId作为FeignClient的name")
        }
        return name
    }

    private fun getContextId(attributes: Map<String, Any>): String {
        val contextId = attributes["contextId"] as String
        if (!StringUtils.hasText(contextId)) {
            throw IllegalStateException("@FeignClient的contextId必须配置")
        }
        return contextId
    }

    private fun getBasePackages(attributes: Map<String, Any>): Array<String> {
        // 构建所有的要进行扫描的包
        val basePackages = ArrayList<String>()
        basePackages += attributes["value"] as Array<String>
        basePackages += attributes["basePackages"] as Array<String>
        basePackages += (attributes["basePackageClasses"] as Array<Class<*>>).map { it.packageName }.toList()
        return basePackages.toTypedArray()
    }

    /**
     * 获取Scanner，去进行FeignClient的扫描，扫描FeignClient的组件...
     */
    private fun getScanner(registry: BeanDefinitionRegistry): ClassPathBeanDefinitionScanner {
        val scanner = ClassPathBeanDefinitionScanner(registry, false)
        scanner.addIncludeFilter(AnnotationTypeFilter(FeignClient::class.java))  // set Include Filter
        return scanner
    }
}