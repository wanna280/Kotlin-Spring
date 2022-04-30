package com.wanna.framework.context.util

import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.beans.factory.support.definition.AbstractBeanDefinition
import com.wanna.framework.beans.factory.support.definition.AnnotatedGenericBeanDefinition
import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.context.annotation.BeanMethod
import com.wanna.framework.context.annotation.ImportBeanDefinitionRegistrar
import com.wanna.framework.context.annotation.BeanDefinitionReader
import com.wanna.framework.context.annotation.BeanNameGenerator
import com.wanna.framework.context.annotation.XmlBeanDefinitionReader
import com.wanna.framework.core.type.AnnotationMetadata
import com.wanna.framework.core.util.BeanUtils
import org.springframework.core.annotation.AnnotatedElementUtils

/**
 * 这是一个配置类的BeanDefinitionReader
 */
open class ConfigurationClassBeanDefinitionReader(
    _registry: BeanDefinitionRegistry, _importBeanNameGenerator: BeanNameGenerator
) {

    // BeanDefinitionRegistry
    private val registry = _registry

    // importBean的beanNameGenerator
    private val importBeanNameGenerator = _importBeanNameGenerator

    /**
     * 从配置类当中加载BeanDefinition
     */
    open fun loadBeanDefinitions(configurationClasses: Collection<ConfigurationClass>) {
        configurationClasses.forEach { configurationClass ->
            if (configurationClass.isImportedBy()) {
                registerBeanDefinition(configurationClass)
            }
            // 将所有的BeanMethod去完成注册
            configurationClass.beanMethods.forEach { beanMethod ->
                loadBeanDefinitionsForBeanMethod(beanMethod)
            }

            // 处理@ImportSource
            loadBeanDefinitionsFromImportedResources(configurationClass.importedSources)

            // 注册ImportBeanDefinitionRegistrar
            loadBeanDefinitionsFromRegistrars(configurationClass.getImportBeanDefinitionRegistrars())
        }
    }

    /**
     * 加载BeanMethod，去将BeanMethod封装成为一个BeanDefinition，并注册BeanDefinition到容器当中
     */
    open fun loadBeanDefinitionsForBeanMethod(beanMethod: BeanMethod) {
        val method = beanMethod.method
        val configClass = beanMethod.configClass
        val beanName: String?

        // 获取到@Bean注解当中的name属性，如果name属性为空的话，那么使用方法名作为beanName
        val beanAnnotation = AnnotatedElementUtils.getMergedAnnotation(method, Bean::class.java)!!
        beanName = beanAnnotation.name.ifBlank { method.name }

        val beanDefinition = RootBeanDefinition()
        // set factoryMethodName, factoryBeanName and factoryMethod
        beanDefinition.setFactoryMethodName(method.name)
        beanDefinition.setFactoryBeanName(configClass.beanName)
        beanDefinition.setResolvedFactoryMethod(method)
        // 设置autowiredMode为构造器注入
        beanDefinition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_CONSTRUCTOR)

        // 注册beanDefinition到容器当中
        registry.registerBeanDefinition(beanName!!, beanDefinition)
    }

    /**
     * 从ImportSource当中去加载BeanDefinition，也就是可以指定xml等类型的配置文件
     */
    open fun loadBeanDefinitionsFromImportedResources(importSources: Map<String, Class<out BeanDefinitionReader>>) {
        importSources.forEach { (resource, readerClass) ->
            // 如果使用默认的Reader
            if (readerClass == BeanDefinitionReader::class.java) {
                XmlBeanDefinitionReader().loadBeanDefinitions(resource)

                // 如果使用了自定义的Reader，那么使用你给定的readerClass去进行加载
            } else {
                BeanUtils.instantiateClass(readerClass).loadBeanDefinitions(resource)
            }
        }
    }

    /**
     * 从BeanDefinitionImportRegistrar当中去加载BeanDefinition
     */
    open fun loadBeanDefinitionsFromRegistrars(registrars: Map<ImportBeanDefinitionRegistrar, AnnotationMetadata>) {
        registrars.forEach { (registrar, annotationMetadata) ->
            registrar.registerBeanDefinitions(annotationMetadata, registry, importBeanNameGenerator)
        }
    }

    open fun registerBeanDefinition(configurationClass: ConfigurationClass) {
        val clazz = configurationClass.configurationClass
        val bd = AnnotatedGenericBeanDefinition(clazz)
        // 生成beanName
        val beanName = importBeanNameGenerator.generateBeanName(bd, registry)
        registry.registerBeanDefinition(beanName, bd)
    }
}