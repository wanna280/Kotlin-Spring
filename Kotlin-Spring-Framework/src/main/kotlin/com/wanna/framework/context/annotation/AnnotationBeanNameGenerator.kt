package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.support.definition.AnnotatedBeanDefinition
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.core.util.ClassUtils
import org.springframework.core.annotation.AnnotatedElementUtils

/**
 * 基于注解的BeanName生成器，它会从Component/ManagedBean/Named等注解当中去找到合适的beanName，
 * 如果没找到，那么将会采用默认的beanName生成方式去进行beanName的生成
 */
open class AnnotationBeanNameGenerator : BeanNameGenerator {
    companion object {
        @JvmField
        val INSTANCE = AnnotationBeanNameGenerator()

        // Spring中的Component注解的类名
        const val COMPONENT_ANNOTATION_CLASSNAME = "com.wanna.framework.context.stereotype.Component"

        // Javax中ManagedBean注解的类名
        const val MANAGED_BEAN_ANNOTATION_CLASSNAME = "javax.annotation.ManagedBean"

        // Javax中Named注解的类名
        const val NAMED_ANNOTATION_CLASSNAME = "javax.inject.Named"
    }

    override fun generateBeanName(beanDefinition: BeanDefinition, registry: BeanDefinitionRegistry): String {
        // 如果它是一个被注解标注的BeanDefinition，那么需要首先从注解当中去进行推断，如果注解当中已经配置了beanName
        // 那么就采用注解当中配置的beanName作为要采用的beanName，如果没有找到合适的beanName，那么就采用默认的生成策略去进行生成
        if (beanDefinition is AnnotatedBeanDefinition) {
            val beanName = determineBeanNameFromAnnotation(beanDefinition)
            if (beanName.isNotBlank()) {
                return beanName
            }
        }
        // 如果它不是一个被注解标注的BeanDefinition，或者从注解当中没有推断出来合适的beanName，那么将会采用默认的方式去生成beanName
        // 默认方式为：将类名的simpleName的首字母小写
        return buildDefaultBeanName(beanDefinition, registry)
    }

    /**
     * 从注解当中去推断出来合适的BeanName
     */
    open fun determineBeanNameFromAnnotation(beanDefinition: AnnotatedBeanDefinition): String {
        if (isCandidateAnnotation(beanDefinition.getBeanClass()!!)) {
            val component = AnnotatedElementUtils.getMergedAnnotation(
                beanDefinition.getBeanClass()!!,
                ClassUtils.getAnnotationClassFromString(
                    COMPONENT_ANNOTATION_CLASSNAME
                )
            )
            val componentAttr = AnnotationAttributesUtils.asAnnotationAttributes(component)
            if (componentAttr != null) {
                val name = componentAttr.getString("value")
                if (name != null) {
                    return name
                }
            }
        }
        return ""
    }

    /**
     * 是否是候选注解？
     */
    private fun isCandidateAnnotation(clazz: Class<*>): Boolean {
        return AnnotatedElementUtils.isAnnotated(clazz, COMPONENT_ANNOTATION_CLASSNAME) ||
                AnnotatedElementUtils.isAnnotated(clazz, MANAGED_BEAN_ANNOTATION_CLASSNAME) ||
                AnnotatedElementUtils.isAnnotated(clazz, NAMED_ANNOTATION_CLASSNAME)
    }

    /**
     * 构建默认的beanName
     */
    open fun buildDefaultBeanName(beanDefinition: BeanDefinition, registry: BeanDefinitionRegistry): String {
        return buildDefaultBeanName(beanDefinition)
    }

    open fun buildDefaultBeanName(beanDefinition: BeanDefinition): String {
        val shortName = ClassUtils.getShortName(beanDefinition.getBeanClass()!!).toCharArray()
        shortName[0] = shortName[0].lowercaseChar()  // 首字母小写
        return String(shortName)
    }
}