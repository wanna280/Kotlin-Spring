package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.support.definition.AnnotatedBeanDefinition
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.core.util.ClassUtils
import java.beans.Introspector

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
        val beanClass = beanDefinition.getBeanClass() ?: throw IllegalStateException("beanClass不能为null")
        if (isCandidateAnnotation(beanClass)) {
            // 获取@Component注解的相关属性
            val component = AnnotatedElementUtils.getMergedAnnotation(
                beanClass,
                ClassUtils.getAnnotationClassFromString(COMPONENT_ANNOTATION_CLASSNAME)
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
     * 是否是候选注解？主要包括三个注解，@Component/@ManagedBean/@Named
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

    /**
     * 构建默认的beanName，默认采用的是首字母小写的方式去进行生成；
     *
     * * 1.如果是一个普通的外层的类，它的类名不含有"$"，因此直接首字母小写即可；
     * * 2.如果它是一个内部类，那么它的className会有一个"$"，需要获取内部类的类名去进行生成。("shortName=外部类名$内部类名")
     *
     * @param beanDefinition BeanDefinition
     */
    protected open fun buildDefaultBeanName(beanDefinition: BeanDefinition): String {
        val beanClassName = beanDefinition.getBeanClassName() ?: throw IllegalStateException("beanClass不能为null")
        val shortName = ClassUtils.getShortName(beanClassName)
        val innerIndex = shortName.lastIndexOf("$")  // 找到内部类"$"字符所在的index(Note: lastIndexOf)
        return if (innerIndex == -1) Introspector.decapitalize(shortName)
        else Introspector.decapitalize(shortName.substring(innerIndex + 1))
    }
}