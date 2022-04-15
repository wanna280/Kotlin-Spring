package com.wanna.framework.context

import com.wanna.framework.beans.factory.support.definition.BeanDefinition

interface ConfigurableListableBeanFactory : ListableBeanFactory, ConfigurableBeanFactory{

    /**
     * 提供针对于ConfigurableListableBeanFactory去获取BeanDefinition的方式
     */
    fun getBeanDefinition(beanName: String): BeanDefinition?

    /**
     * 完成剩下所有单实例Bean的实例化和初始化
     */
    fun preInstantiateSingletons()
}