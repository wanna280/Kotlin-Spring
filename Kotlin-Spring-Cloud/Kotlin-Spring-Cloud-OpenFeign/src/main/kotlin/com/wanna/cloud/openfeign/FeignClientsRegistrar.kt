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
 *
 * @see EnableFeignClients
 * @see FeignClient
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
            // 注册默认的配置类列表...这些配置类是要apply给全部的FeignClient的，因此需要加上"default."前缀
            val name = NamedContextFactory.DEFAULT_PREFIX + annotationMetadata.getClassName()
            val configurations = attributes["defaultConfiguration"] as Array<Class<*>>
            registerClientConfiguration(registry, name, configurations)

            // 处理@EnableFeignClients当中进行扫描的FeignClient
            registerFeignClients(registry, attributes)
        }
    }

    private fun registerFeignClients(registry: BeanDefinitionRegistry, attributes: Map<String, Any>) {
        val basePackages = getBasePackages(attributes)
        val candidateComponents = LinkedHashSet<BeanDefinition>()
        val scanner = getScanner(registry)
        // 处理@EnableFeignClients扫描到的组件的列表
        basePackages.forEach { candidateComponents += scanner.findCandidateComponents(it) }

        // 处理@EnableFeignClients上配置的clients属性，全部合并到候选的组件当中去
        val clients = attributes["clients"] as Array<Class<*>>
        val clientsBeanDefinitions = clients.map { AnnotatedGenericBeanDefinition(it) }.toList()
        candidateComponents += clientsBeanDefinitions

        candidateComponents.forEach {
            if (it is AnnotatedBeanDefinition) {
                val metadata = it.getMetadata()
                val clientAttributes = metadata.getAnnotationAttributes(FeignClient::class.java)
                val clientName = getClientName(clientAttributes)
                // 注册@FeignClient上对于当前的FeignClient的配置类列表Configurations
                registerClientConfiguration(registry, clientName, clientAttributes["configuration"] as Array<Class<*>>)

                // 解析@FeignClient当中的相关属性，从而去注册FeignClient
                registerFeignClient(registry, metadata, clientAttributes)
            }
        }
    }

    /**
     * 注册FeignClient的相关配置类，会自动apply给对应的FeignClient
     */
    private fun registerClientConfiguration(
        registry: BeanDefinitionRegistry, name: String, configurations: Array<Class<*>>
    ) {
        val beanDefinition = GenericBeanDefinition(FeignClientSpecification::class.java)
        beanDefinition.setInstanceSupplier { FeignClientSpecification(name, configurations) }
        registry.registerBeanDefinition(name + "." + FeignClientSpecification::class.java.name, beanDefinition)
    }

    /**
     * 解析一个@FeignClient注解，将其封装成为FeignClientFactoryBean，并注册到容器当中
     */
    private fun registerFeignClient(
        registry: BeanDefinitionRegistry, metadata: AnnotationMetadata, attributes: Map<String, Any>
    ) {
        val clientName = getClientName(attributes)
        val type =
            if (metadata is StandardAnnotationMetadata) metadata.clazz
            else ClassUtils.forName<Any>(metadata.getClassName())

        val factoryBean = FeignClientFactoryBean()
        factoryBean.name = clientName
        factoryBean.type = type
        factoryBean.contextId = getContextId(attributes)
        factoryBean.url = attributes["url"] as String
        factoryBean.path = attributes["path"] as String

        // fallback and fallbackFactory
        val fallback = attributes["fallback"] as Class<*>
        if (fallback != Void::class.java) {
            factoryBean.fallback = fallback
        }
        val fallbackFactory = attributes["fallbackFactory"] as Class<*>
        if (fallbackFactory != Void::class.java) {
            factoryBean.fallbackFactory = fallbackFactory
        }
        // setBeanFactory
        if (registry is ConfigurableBeanFactory) {
            factoryBean.setBeanFactory(registry)
        }

        val beanDefinition = GenericBeanDefinition(factoryBean.type)
        beanDefinition.setInstanceSupplier(factoryBean::getTarget)

        // 注册BeanDefinition到registry当中
        registry.registerBeanDefinition(clientName, beanDefinition)
    }

    /**
     * 获取Scanner，去进行FeignClient的扫描，扫描FeignClient的组件...
     */
    private fun getScanner(registry: BeanDefinitionRegistry): ClassPathBeanDefinitionScanner {
        val scanner = ClassPathBeanDefinitionScanner(registry, false)
        scanner.addIncludeFilter(AnnotationTypeFilter(FeignClient::class.java))  // set Include Filter
        return scanner
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

    /**
     * 获取FeignClient要扫描的包的列表
     *
     * @param attributes @FeignClient的注解属性
     * @return 解析到的@FeignClient当中要扫描单独包的列表
     */
    private fun getBasePackages(attributes: Map<String, Any>): Array<String> {
        // 构建所有的要进行扫描的包
        val basePackages = ArrayList<String>()
        basePackages += attributes["value"] as Array<String>
        basePackages += attributes["basePackages"] as Array<String>
        basePackages += (attributes["basePackageClasses"] as Array<Class<*>>).map { it.packageName }.toList()
        return basePackages.toTypedArray()
    }
}