package com.wanna.framework.beans.factory.config

import com.wanna.framework.beans.factory.ListableBeanFactory
import com.wanna.framework.beans.factory.config.ConfigurableBeanFactory
import com.wanna.framework.beans.factory.support.AutowireCapableBeanFactory
import com.wanna.framework.beans.factory.support.DependencyDescriptor
import com.wanna.framework.beans.factory.support.definition.BeanDefinition

interface ConfigurableListableBeanFactory : ListableBeanFactory, ConfigurableBeanFactory, AutowireCapableBeanFactory {

    /**
     * 提供针对于ConfigurableListableBeanFactory去获取BeanDefinition的方式
     */
    fun getBeanDefinition(beanName: String): BeanDefinition?

    /**
     * 注册一个可以被解析的依赖，注册之后，后续可以通过Autowire进行湖区
     */
    fun registerResolvableDependency(dependencyType: Class<*>, autowireValue: Any)

    /**
     * 判断一个Bean是否是Autowire候选的Bean
     */
    fun isAutowireCandidate(beanName: String, descriptor: DependencyDescriptor): Boolean

    /**
     * 完成剩下所有单实例Bean的实例化和初始化
     */
    fun preInstantiateSingletons()
}