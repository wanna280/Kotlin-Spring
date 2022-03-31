package com.wanna.framework.context.util

import com.wanna.framework.beans.BeanDefinition
import com.wanna.framework.context.AnnotationAttributes
import com.wanna.framework.context.AnnotationAttributesUtils
import com.wanna.framework.context.BeanDefinitionRegistry
import com.wanna.framework.context.ComponentScanMetadata
import com.wanna.framework.context.annotations.ComponentScan
import com.wanna.framework.core.environment.Environment
import org.springframework.core.annotation.AnnotatedElementUtils
import java.util.function.Predicate

/**
 * 这是一个配置类的解析器，用来扫描配置类相关的注解，将其注册到容器当中
 */
class ConfigurationClassParser(
    _registry: BeanDefinitionRegistry,
    _environment: Environment,
    _classLoader: ClassLoader
) {

    // BeanDefinition的注册中心
    val registry: BeanDefinitionRegistry = _registry

    // 容器对象对应的环境对象
    val environment: Environment = _environment

    // 类加载器
    val classLoader: ClassLoader = _classLoader

    // ComponentScan注解的解析器
    private val parser: ComponentScanAnnotationParser =
        ComponentScanAnnotationParser(_registry, _environment, _classLoader)

    // 维护了扫描出来的ConfigurationClass的集合
    val configurationClasses = LinkedHashMap<ConfigurationClass, ConfigurationClass>()

    /**
     * 获取解析完成的配置类列表
     */
    fun getConfigurationClasses(): MutableSet<ConfigurationClass> {
        return configurationClasses.keys
    }

    fun parse() {
        parse(registry.getBeanDefinitions())
    }

    fun parse(candidates: Collection<BeanDefinition>) {
        candidates.forEach {
            parse(it)
        }
    }

    fun parse(beanDefinition: BeanDefinition) {
        processConfigurationClass(ConfigurationClass(beanDefinition)) {
            it.startsWith("java.")
        }
    }

    private fun processConfigurationClass(configurationClass: ConfigurationClass, filter: Predicate<String>) {
        if (configurationClasses.containsKey(configurationClass)) {
            return
        }

        // 将配置类注册到已有的配置类当中
        configurationClasses[configurationClass] = configurationClass

        doProcessConfigurationClass(configurationClass, filter)
    }

    private fun doProcessConfigurationClass(configurationClass: ConfigurationClass, filter: Predicate<String>) {

        // 处理ComponentScan注解
        processComponentScans(configurationClass)
    }

    private fun processComponentScans(configurationClass: ConfigurationClass) {
        // 找到注解上的ComponentScan注解
        val componentScans = AnnotatedElementUtils.findAllMergedAnnotations(
            configurationClass.configurationClass,
            ComponentScan::class.java
        )
        val attributesSet = AnnotationAttributesUtils.asAnnotationAttributesSet(componentScans)
        // 遍历标注的所有CompoentScan注解
        attributesSet.forEach { attr ->
            if (componentScans.isNotEmpty()) {
                // 扫描该ComponentScan注解
                val beanDefinitions = parser.parse(ComponentScanMetadata(configurationClass, attr!!))

                // 遍历所有的BeanDefinition，完成注册
                beanDefinitions.forEach { bd ->
                    registry.registerBeanDefinition(bd.beanName, bd)
                }
            }
        }


    }
}