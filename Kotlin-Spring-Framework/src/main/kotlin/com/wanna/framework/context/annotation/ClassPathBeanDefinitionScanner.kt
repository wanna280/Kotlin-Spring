package com.wanna.framework.context.annotation

import com.wanna.framework.context.stereotype.Component
import com.wanna.framework.beans.factory.support.BeanDefinitionHolder
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.definition.ScannedGenericBeanDefinition
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.environment.EnvironmentCapable
import com.wanna.framework.core.environment.StandardEnvironment
import com.wanna.framework.core.util.AnnotationConfigUtils
import com.wanna.framework.core.util.ClassDiscoveryUtils
import org.springframework.core.annotation.AnnotatedElementUtils

/**
 * 这是ClassPath下的BeanDefinition的Scanner，负责完成指定的包下的全部配置类的扫描
 */
open class ClassPathBeanDefinitionScanner(private val registry: BeanDefinitionRegistry) {

    // beanNameGenerator，可以允许外部访问，直接进行设置，默认为支持注解版的BeanNameGenerator
    private var beanNameGenerator: BeanNameGenerator = AnnotationBeanNameGenerator.INSTANCE

    // environment，利用允许外部访问，直接去进行设置
    private var environment: Environment = this.getOrDefaultEnvironment(registry)

    // 是否包含注解版的配置？如果开启了，使用它进行扫描时，就会往容器中注册注解的通用处理器
    private var includeAnnotationConfig: Boolean = true

    /**
     * 扫描指定的包中的BeanDefinition，并注册到容器当中，返回值为扫描到的BeanDefinition的数量
     */
    open fun scan(vararg packages: String): Int {
        val beforeCount = registry.getBeanDefinitionCount()  // before Count
        // 完成指定的所有的包的扫描，并直接注册到容器当中
        doScan(*packages)

        // 如果需要引入注解处理的相关配置
        if (includeAnnotationConfig) {
            AnnotationConfigUtils.registerAnnotationConfigProcessors(registry)
        }
        return registry.getBeanDefinitionCount() - beforeCount  // return modifyCount=afterCount-beforeCount
    }

    /**
     * doScan，获取到指定的包下的所有BeanDefinition
     */
    open fun doScan(vararg packages: String): Set<BeanDefinitionHolder> {
        val beanDefinitions = HashSet<BeanDefinitionHolder>()
        // 获取要进行扫描的包中的所有候选BeanDefinition
        val candidateComponents = findCandidateComponents(*packages)
        candidateComponents.filter { isCandidate(it.getBeanClass()) }
            .forEach { beanDefinition ->
                // 利用beanNameGenerator给beanDefinition生成beanName，并注册到BeanDefinitionRegistry当中
                val beanName = beanNameGenerator.generateBeanName(beanDefinition, registry)
                registry.registerBeanDefinition(beanName, beanDefinition)

                // 加入到扫描到的BeanDefinition列表当中，封装成为BeanDefinitionHolder，让调用方可以获取到beanName
                beanDefinitions += BeanDefinitionHolder(beanDefinition, beanName)
            }
        return beanDefinitions
    }

    /**
     * 从指定的包下，扫描出来所有的BeanDefinitions列表
     */
    private fun findCandidateComponents(vararg packages: String): Set<BeanDefinition> {
        return ClassDiscoveryUtils.scan(*packages).map { ScannedGenericBeanDefinition(it) }.toSet()
    }

    /**
     * 是否是候选的要导入的组件？
     * 如果它标注了Component注解、并且它不是一个注解、并且类名不是以java.开头的，那么它就是符合要求的组件
     */
    protected open fun isCandidate(clazz: Class<*>?): Boolean {
        return clazz != null && !clazz.isAnnotation && !clazz.name.startsWith("java.")
                && AnnotatedElementUtils.isAnnotated(clazz, Component::class.java)
    }

    /**
     * 获取或者是创建一个默认的Environment，
     * (1)如果Registry可以获取到Environment，那么直接从Registry当中去获取到Environment对象;
     * (2)如果获取不到，那么就创建一个默认的Environment
     */
    private fun getOrDefaultEnvironment(registry: BeanDefinitionRegistry): Environment {
        return if (registry is EnvironmentCapable) registry.getEnvironment() else StandardEnvironment()
    }

    open fun setEnvironment(environment: Environment) {
        this.environment = environment
    }

    open fun setBeanNameGenerator(beanNameGenerator: BeanNameGenerator?) {
        this.beanNameGenerator = beanNameGenerator ?: AnnotationBeanNameGenerator.INSTANCE
    }

    open fun setIncludeAnnotationConfig(includeAnnotationConfig: Boolean) {
        this.includeAnnotationConfig = includeAnnotationConfig
    }
}