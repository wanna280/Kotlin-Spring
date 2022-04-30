package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.core.type.AnnotationMetadata

/**
 * 这是一个给容器中导入组件的注册器
 */
interface ImportBeanDefinitionRegistrar {

    /**
     * 可以通过这个方法，拿到registry去给BeanDefinition注册中心当中批量注册组件
     *
     * @param annotationMetadata 目标类上的注解信息
     * @param registry BeanDefinitionRegistry
     * @param beanNameGenerator beanName的生成器
     */
    fun registerBeanDefinitions(
        annotationMetadata: AnnotationMetadata,
        registry: BeanDefinitionRegistry,
        beanNameGenerator: BeanNameGenerator
    ) {
        registerBeanDefinitions(annotationMetadata, registry)
    }

    /**
     * 可以通过这个方法，拿到registry去给BeanDefinition注册中心当中批量注册组件
     *
     * @see registerBeanDefinitions
     */
    fun registerBeanDefinitions(annotationMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {

    }
}