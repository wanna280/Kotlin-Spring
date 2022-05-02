package com.wanna.framework.context.annotation

import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.beans.factory.support.BeanDefinitionHolder
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.core.environment.Environment
import org.springframework.core.annotation.AnnotatedElementUtils

/**
 * 这是完成ComponentScan注解的扫描的解析器，负责将@ComponentScan注解当中配置的属性去进行解析，并完成ComponentScan的组件的扫描
 *
 * @see ClassPathBeanDefinitionScanner
 */
class ComponentScanAnnotationParser(
    private val registry: BeanDefinitionRegistry,
    private val environment: Environment,
    private val classLoader: ClassLoader,
    private val componentScanBeanNameGenerator: BeanNameGenerator
) {
    fun parse(componentScanMetadata: ComponentScanMetadata): Set<BeanDefinitionHolder> {
        val scanner = ClassPathBeanDefinitionScanner(registry)

        // 设置beanNameGenerator
        scanner.setBeanNameGenerator(componentScanBeanNameGenerator)

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
            clazz, Component::class.java
        ) && !clazz.isAnnotation && !clazz.name.startsWith("java.")
    }
}