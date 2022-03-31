package com.wanna.framework.context

/**
 * 这是一个给容器中导入组件的注册器
 */
interface ImportBeanDefinitionRegistrar {

    fun registerBeanDefinitionNames(registry: BeanDefinitionRegistry) {

    }
}