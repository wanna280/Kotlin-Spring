package com.wanna.framework.context.util

import com.wanna.framework.beans.definition.BeanDefinition
import com.wanna.framework.beans.definition.RootBeanDefinition
import com.wanna.framework.beans.annotations.Component
import com.wanna.framework.context.BeanDefinitionRegistry
import com.wanna.framework.context.ComponentScanMetadata
import com.wanna.framework.context.annotations.BeanNameGenerator
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.util.ClassDiscoveryUtils
import org.springframework.core.annotation.AnnotatedElementUtils

/**
 * 这是完成ComponentScan注解的扫描的解析器
 */
class ComponentScanAnnotationParser(
    _registry: BeanDefinitionRegistry,
    _environment: Environment,
    _classLoader: ClassLoader,
    _componentScanBeanNameGenerator: BeanNameGenerator
) {
    // BeanDefinition的注册中心
    val registry: BeanDefinitionRegistry = _registry

    // 容器对象对应的环境对象
    val environment: Environment = _environment

    // 类加载器
    val classLoader: ClassLoader = _classLoader

    // componentScan的beanNameGenerator
    val componentScanBeanNameGenerator = _componentScanBeanNameGenerator

    fun parse(componentScanMetadata: ComponentScanMetadata): Set<BeanDefinition> {
        val scanner = ClassPathBeanDefinitionScanner(registry, environment)

        // 设置beanNameGenerator
        scanner.beanNameGenerator = componentScanBeanNameGenerator

        val attributes = componentScanMetadata.attributes
        val basePackages = attributes.getStringArray("basePackages")

        return scanner.doScan(basePackages!!)
    }

    /**
     * 是否是候选的要导入的组件？
     * 如果它标注了Component注解、并且它不是一个注解、并且类名不是以java.开头的
     */
    private fun isCandidate(clazz: Class<*>): Boolean {
        return AnnotatedElementUtils.isAnnotated(
            clazz,
            Component::class.java
        ) && !clazz.isAnnotation && !clazz.name.startsWith("java.")
    }
}