package com.wanna.framework.context.annotations

import com.wanna.framework.context.BeanDefinitionRegistry
import com.wanna.framework.util.AnnotationConfigUtils

/**
 * 这是一个注解的BeanDefinitionReader
 */
class AnnotatedBeanDefinitionReader(val beanDefinitionRegistry: BeanDefinitionRegistry) {

    init {
        // 注册AnnotationConfig相关的PostProcessor
        AnnotationConfigUtils.registerAnnotationConfigProcessors(beanDefinitionRegistry)
    }

}