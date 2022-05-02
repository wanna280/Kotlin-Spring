package com.wanna.boot.autoconfigure

import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.context.annotation.BeanNameGenerator
import com.wanna.framework.context.annotation.ImportBeanDefinitionRegistrar
import com.wanna.framework.core.type.AnnotationMetadata

/**
 * 这是一个给SpringBoot当中去进行自动配置包的类
 */
class AutoConfigurationPackages {

    /**
     * 这是一个BeanDefinitionRegistrar，负责给容器中导入组件
     */
    class Registrar : ImportBeanDefinitionRegistrar {
        override fun registerBeanDefinitions(
            annotationMetadata: AnnotationMetadata,
            registry: BeanDefinitionRegistry,
            beanNameGenerator: BeanNameGenerator
        ) {

        }
    }
}