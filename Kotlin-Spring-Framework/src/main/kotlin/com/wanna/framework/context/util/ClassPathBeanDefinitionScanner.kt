package com.wanna.framework.context.util

import com.wanna.framework.beans.annotations.Component
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.beans.factory.support.definition.ScannedGenericBeanDefinition
import com.wanna.framework.context.BeanDefinitionRegistry
import com.wanna.framework.context.annotations.BeanNameGenerator
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.util.ClassDiscoveryUtils
import org.springframework.core.annotation.AnnotatedElementUtils

/**
 * 这是ClassPath下的BeanDefinition的Scanner
 */
open class ClassPathBeanDefinitionScanner(_registry: BeanDefinitionRegistry, _environment: Environment) {

    // BeanDefinition的注册中心
    val registry = _registry

    // beanNameGenerator
    var beanNameGenerator: BeanNameGenerator? = null

    // environment
    val environment: Environment = _environment

    fun doScan(packages: Array<String>): Set<BeanDefinition> {
        val beanDefinitions = HashSet<BeanDefinition>()
        // 获取要进行扫描的包中的所有类
        ClassDiscoveryUtils.scan(*packages)
            .filter { isCandidate(it) }
            .forEach {
                val genericBeanDefinition = ScannedGenericBeanDefinition(it)
                val beanName = beanNameGenerator!!.generateBeanName(genericBeanDefinition, registry)
                registry.registerBeanDefinition(beanName, genericBeanDefinition)
            }
        return beanDefinitions
    }

    /**
     * 是否是候选的要导入的组件？
     * 如果它标注了Component注解、并且它不是一个注解、并且类名不是以java.开头的
     */
    protected fun isCandidate(clazz: Class<*>): Boolean {
        return AnnotatedElementUtils.isAnnotated(
            clazz,
            Component::class.java
        ) && !clazz.isAnnotation && !clazz.name.startsWith("java.")
    }
}