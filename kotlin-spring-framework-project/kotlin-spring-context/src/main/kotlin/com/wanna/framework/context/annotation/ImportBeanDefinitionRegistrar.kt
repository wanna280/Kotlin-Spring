package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.BeanNameGenerator
import com.wanna.framework.core.type.AnnotationMetadata

/**
 * 这是一个可以通过编码的方式, 从而实现给SpringBeanFactory当中批量导入组件的注册器,
 * SpringBeanFactory, 会自动将registry传递给你, 你可以自行完成BeanDefinition的注册工作;
 * 类似地：你也可以使用@Import注解, 或者是ImportBeanDefinitionRegistrar的方式给容器当中批量导入组件
 *
 * @see Import
 * @see ImportBeanDefinitionRegistrar
 */
interface ImportBeanDefinitionRegistrar {

    /**
     * 可以通过这个方法, 拿到registry去给BeanDefinition注册中心当中批量注册组件;
     *
     * ### Note:
     * 这个方法支持使用beanNameGenerator(SpringBeanFactory会将BeanNameGenerator传递给你)
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
     * 可以通过这个方法, 拿到registry去给BeanDefinition注册中心当中批量注册组件
     *
     * @see registerBeanDefinitions
     */
    fun registerBeanDefinitions(annotationMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {

    }
}