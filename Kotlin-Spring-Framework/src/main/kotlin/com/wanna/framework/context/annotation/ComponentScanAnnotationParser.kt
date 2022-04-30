package com.wanna.framework.context.annotation

import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.beans.factory.support.BeanDefinitionHolder
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.core.environment.Environment
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

    fun parse(componentScanMetadata: ComponentScanMetadata): Set<BeanDefinitionHolder> {
        val scanner = ClassPathBeanDefinitionScanner(registry)

        // 设置beanNameGenerator
        scanner.beanNameGenerator = componentScanBeanNameGenerator

        val attributes = componentScanMetadata.attributes
        val basePackages = attributes.getStringArray("basePackages")

        // 使用类路径下的BeanDefinitionScanner去进行扫描
        return scanner.doScan(*basePackages!!)
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