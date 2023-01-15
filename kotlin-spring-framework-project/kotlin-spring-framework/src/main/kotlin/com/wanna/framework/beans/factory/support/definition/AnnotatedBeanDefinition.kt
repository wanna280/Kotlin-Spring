package com.wanna.framework.beans.factory.support.definition

import com.wanna.framework.core.type.AnnotationMetadata

/**
 * 这是一个注解标注的BeanDefinition, 那么需要获取到它标注了哪些注解的相关信息
 *
 * @see AnnotationMetadata
 */
interface AnnotatedBeanDefinition : BeanDefinition {

    /**
     * 获取注解的相Metadata信息
     */
    fun getMetadata(): AnnotationMetadata
}