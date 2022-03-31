package com.wanna.framework.context.util

import com.wanna.framework.beans.annotations.Bean
import com.wanna.framework.beans.definition.AnnotatedGenericBeanDefinition
import com.wanna.framework.context.BeanDefinitionRegistry
import com.wanna.framework.context.BeanMethod
import com.wanna.framework.context.ImportBeanDefinitionRegistrar
import com.wanna.framework.context.annotations.BeanDefinitionReader
import com.wanna.framework.context.annotations.BeanNameGenerator
import com.wanna.framework.context.annotations.XmlBeanDefinitionReader
import com.wanna.framework.util.ClassUtils
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
            loadBeanDefinitionsFromRegistrars(configurationClass.importBeanDefinitionRegistrars.keys)
        }
    }

    /**
     * 加载BeanMethod，去注册BeanDefinition
     */
    open fun loadBeanDefinitionsForBeanMethod(beanMethod: BeanMethod) {
        val method = beanMethod.method
        var beanName: String? = null
        val bean = AnnotatedElementUtils.getMergedAnnotation(method, Bean::class.java)!!
        beanName = bean.name.ifBlank { method.name }

        // 注册beanDefinition到容器当中
        registry.registerBeanDefinition(beanName!!, AnnotatedGenericBeanDefinition(method.returnType))
    }

    open fun loadBeanDefinitionsFromImportedResources(importSources: Map<String, Class<out BeanDefinitionReader>>) {
        importSources.forEach { resource, readerClass ->
            // 如果使用默认的Reader
            if (readerClass == BeanDefinitionReader::class.java) {
                XmlBeanDefinitionReader().loadBeanDefinitions(resource)

                // 如果使用了自定义的Reader
            } else {
                ClassUtils.newInstance(readerClass).loadBeanDefinitions(resource)
            }
        }
    }

    open fun loadBeanDefinitionsFromRegistrars(registrars: Collection<ImportBeanDefinitionRegistrar>) {
        registrars.forEach {

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