package com.wanna.framework.context

interface ConfigurableListableBeanFactory : ListableBeanFactory, ConfigurableBeanFactory, BeanDefinitionRegistry {

    /**
     * 完成剩下所有单实例Bean的实例化和初始化
     */
    fun preInstantiateSingletons()
}