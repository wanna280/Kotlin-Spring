package com.wanna.framework.context.annotations

import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.context.annotations.AnnotationBeanNameGenerator

/**
 * 基于全限定名的支持注解方式的BeanName生成器
 */
class FullyQualifiedAnnotationBeanNameGenerator : AnnotationBeanNameGenerator() {
    companion object {
        @JvmField
        val INSTANCE = FullyQualifiedAnnotationBeanNameGenerator()
    }

    /**
     * 构建默认的beanName，采用全类名作为beanName
     */
    override fun buildDefaultBeanName(beanDefinition: BeanDefinition): String {
        return beanDefinition.getBeanClass()!!.name
    }
}