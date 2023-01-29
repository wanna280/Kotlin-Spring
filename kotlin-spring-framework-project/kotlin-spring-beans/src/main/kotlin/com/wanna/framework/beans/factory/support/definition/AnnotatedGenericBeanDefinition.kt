package com.wanna.framework.beans.factory.support.definition

import com.wanna.framework.core.type.AnnotationMetadata
import com.wanna.framework.util.ClassUtils

/**
 * 这是一个被注解标注的通用的BeanDefinition
 */
open class AnnotatedGenericBeanDefinition protected constructor() : AnnotatedBeanDefinition, GenericBeanDefinition() {

    /**
     * AnnotationMetadata(维护注解相关信息)
     */
    private var metadata: AnnotationMetadata? = null

    /**
     * 基于beanClass的方式去进行构建
     *
     * @param beanClass beanClass
     */
    constructor(beanClass: Class<*>) : this() {
        this.metadata = AnnotationMetadata.introspect(beanClass)
        this.setBeanClass(beanClass)
    }

    /**
     * 基于AnnotationMetadata的方式去进行构建
     *
     * @param metadata metadata
     */
    constructor(metadata: AnnotationMetadata) : this() {
        this.metadata = metadata
        this.setBeanClassName(metadata.getClassName())  // set beanClassName
    }

    override fun getMetadata() = metadata ?: throw IllegalStateException("Metadata is null")
}