package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.support.definition.BeanDefinition

/**
 * 基于全限定名的支持注解方式的BeanName生成器
 */
open class FullyQualifiedAnnotationBeanNameGenerator : AnnotationBeanNameGenerator() {
    companion object {
        @JvmField
        val INSTANCE = FullyQualifiedAnnotationBeanNameGenerator()
    }

    /**
     * 构建默认的beanName, 采用全类名作为beanName
     */
    override fun buildDefaultBeanName(beanDefinition: BeanDefinition): String {
        return beanDefinition.getBeanClassName() ?: throw IllegalStateException("beanClassName不能为空")
    }
}