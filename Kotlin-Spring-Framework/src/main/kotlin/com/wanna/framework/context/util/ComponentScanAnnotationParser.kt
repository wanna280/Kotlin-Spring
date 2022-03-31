package com.wanna.framework.context.util

import com.wanna.framework.beans.BeanDefinition
import com.wanna.framework.beans.RootBeanDefinition
import com.wanna.framework.beans.annotations.Component
import com.wanna.framework.context.BeanDefinitionRegistry
import com.wanna.framework.context.ComponentScanMetadata
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.util.ClassDiscoveryUtils
import org.springframework.core.annotation.AnnotatedElementUtils

/**
 * 这是完成ComponentScan注解的扫描的解析器
 */
class ComponentScanAnnotationParser(
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

    fun parse(componentScanMetadata: ComponentScanMetadata): Set<BeanDefinition> {
        val attributes = componentScanMetadata.attributes
        val beanDefinitions = HashSet<BeanDefinition>()

        val basePackages = attributes.getStringArray("basePackages")

        // 获取要进行扫描的包中的所有类
        ClassDiscoveryUtils.scan(*(basePackages!!))
            .filter { isCandidate(it) }
            .forEach { beanDefinitions += RootBeanDefinition(it.simpleName, it) }

        return beanDefinitions
    }

    /**
     * 是否是候选的要导入的组件？
     */
    fun isCandidate(clazz: Class<*>): Boolean {
        return AnnotatedElementUtils.isAnnotated(
            clazz,
            Component::class.java
        ) && !clazz.isAnnotation && !clazz.simpleName.startsWith("java.")
    }
}