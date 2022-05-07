package com.wanna.framework.beans.factory.support.definition

import com.wanna.framework.core.type.AnnotationMetadata
import com.wanna.framework.core.type.StandardAnnotationMetadata

/**
 * 这是一个被注解标注的通用的BeanDefinition
 */
open class AnnotatedGenericBeanDefinition(_beanClass: Class<*>) : AnnotatedBeanDefinition,
    GenericBeanDefinition(_beanClass) {

    // AnnotationMetadata(维护注解相关信息)
    private var metadata: AnnotationMetadata = StandardAnnotationMetadata(_beanClass)

    override fun getMetadata() = metadata
}