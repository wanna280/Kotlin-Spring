package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.support.definition.AnnotatedBeanDefinition
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.core.annotation.MergedAnnotation
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.AnnotationConfigUtils
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.StringUtils
import java.beans.Introspector

/**
 * 基于注解的BeanName生成器, 它会从Component/ManagedBean/Named等注解当中去找到合适的beanName,
 * 如果没找到, 那么将会采用默认的beanName生成方式去进行beanName的生成
 */
open class AnnotationBeanNameGenerator : BeanNameGenerator {
    companion object {

        /**
         * 对外暴露单例对象
         */
        @JvmField
        val INSTANCE = AnnotationBeanNameGenerator()

        /**
         * Spring中的Component注解的类名
         */
        const val COMPONENT_ANNOTATION_CLASSNAME = "com.wanna.framework.context.stereotype.Component"

        /**
         * Javax中ManagedBean注解的类名
         */
        const val MANAGED_BEAN_ANNOTATION_CLASSNAME = "javax.annotation.ManagedBean"

        /**
         * Javax中Named注解的类名
         */
        const val NAMED_ANNOTATION_CLASSNAME = "javax.inject.Named"
    }

    /**
     * 为给定的BeanDefinition, 去生成出来合适的beanName
     *
     * @param beanDefinition BeanDefinition
     * @param registry Registry
     * @return beanName
     */
    override fun generateBeanName(beanDefinition: BeanDefinition, registry: BeanDefinitionRegistry): String {
        // 如果它是一个被注解标注的BeanDefinition, 那么需要首先从注解当中去进行推断, 如果注解当中已经配置了beanName
        // 那么就采用注解当中配置的beanName作为要采用的beanName, 如果没有找到合适的beanName, 那么就采用默认的生成策略去进行生成
        if (beanDefinition is AnnotatedBeanDefinition) {
            val beanName = determineBeanNameFromAnnotation(beanDefinition)
            if (StringUtils.hasText(beanName)) {
                return beanName!!
            }
        }
        // 如果它不是一个被注解标注的BeanDefinition, 或者从注解当中没有推断出来合适的beanName, 那么将会采用默认的方式去生成beanName
        // 默认方式为：将类名的simpleName的首字母小写
        return buildDefaultBeanName(beanDefinition, registry)
    }

    /**
     * 从注解的Metadata当中去推断出来合适的BeanName
     *
     * @param beanDefinition 待解析BeanName的BeanDefinition
     * @return 根据Metadata解析到的beanName, 没解析到return null
     */
    @Nullable
    open fun determineBeanNameFromAnnotation(beanDefinition: AnnotatedBeanDefinition): String? {
        val metadata = beanDefinition.getMetadata()
        val annotationTypes = metadata.getAnnotationTypes()

        var beanName: String? = null

        for (annotationType in annotationTypes) {
            val attributes = AnnotationConfigUtils.attributesFor(metadata, annotationType)
            if (attributes != null) {

                // 获取该注解的MetaAnnotation类型列表
                val metaAnnotationTypes = metadata.getMetaAnnotationTypes(annotationType)

                // 检查是否标注了@Component注解...
                if (isStereotypeWithNameValue(annotationType, metaAnnotationTypes, attributes)) {
                    // 获取到该注解的value属性...
                    val strVal = attributes[MergedAnnotation.VALUE]

                    // 如果是String, 才需要去进行收集起来...不是String直接跳过...
                    if (strVal is String) {
                        // 如果解析到了多个beanName的话...return false
                        if (beanName != null && strVal == beanName) {
                            throw IllegalStateException("Stereotype annotations suggest inconsistent '$beanName' versus '$strVal'")
                        }
                        beanName = strVal
                    }
                }
            }
        }
        return beanName
    }

    /**
     * 是否要标注Name相关的注解(包括三个注解Spring的Component, javax.inject.Named, javax.annotation.ManagedBean)
     *
     * @param annotationType annotationType
     * @param metaAnnotationTypes metaAnnotationTypes
     * @param attributes attributes
     * @return 如果有它Name/Value注解, 那么return true; 否则return false
     */
    private fun isStereotypeWithNameValue(
        annotationType: String,
        metaAnnotationTypes: Set<String>,
        @Nullable attributes: Map<String, Any>?
    ): Boolean {
        val isStereotype = (annotationType == COMPONENT_ANNOTATION_CLASSNAME
                || metaAnnotationTypes.contains(COMPONENT_ANNOTATION_CLASSNAME)
                || annotationType == NAMED_ANNOTATION_CLASSNAME
                || annotationType == MANAGED_BEAN_ANNOTATION_CLASSNAME)
        return isStereotype && attributes != null && attributes.containsKey(MergedAnnotation.VALUE)
    }

    /**
     * 构建默认的beanName, 默认使用类名首字母小写去作为beanName, 子类可以通过重写实现自定义的构建默认的beanName的方式
     *
     * @param beanDefinition BeanDefinition
     * @param registry registry
     */
    open fun buildDefaultBeanName(beanDefinition: BeanDefinition, registry: BeanDefinitionRegistry): String {
        return buildDefaultBeanName(beanDefinition)
    }

    /**
     * 构建默认的beanName, 默认采用的是首字母小写的方式去进行生成;
     *
     * * 1.如果是一个普通的外层的类, 它的类名不含有"$", 因此直接首字母小写即可;
     * * 2.如果它是一个内部类, 那么它的className会有一个"$", 需要获取内部类的类名去进行生成.("shortName=外部类名$内部类名")
     *
     * @param beanDefinition BeanDefinition
     */
    protected open fun buildDefaultBeanName(beanDefinition: BeanDefinition): String {
        val beanClassName = beanDefinition.getBeanClassName() ?: throw IllegalStateException("beanClassName不能为null")
        val shortName = ClassUtils.getShortName(beanClassName)
        val innerIndex = shortName.lastIndexOf("$")  // 找到内部类"$"字符所在的index(Note: lastIndexOf)
        return if (innerIndex == -1) Introspector.decapitalize(shortName)
        else Introspector.decapitalize(shortName.substring(innerIndex + 1))
    }
}