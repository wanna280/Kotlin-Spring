package com.wanna.framework.beans.factory.config

import com.wanna.framework.beans.factory.ListableBeanFactory
import com.wanna.framework.beans.factory.support.AutowireCapableBeanFactory
import com.wanna.framework.beans.factory.support.DependencyDescriptor
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.context.exception.NoSuchBeanDefinitionException

interface ConfigurableListableBeanFactory : ListableBeanFactory, ConfigurableBeanFactory, AutowireCapableBeanFactory {

    /**
     * 获取BeanDefinition，一定能获取到，如果获取不到直接抛出异常；
     *
     * @throws NoSuchBeanDefinitionException 如果没有找到这样的BeanDefinition异常
     */
    fun getBeanDefinition(beanName: String): BeanDefinition

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