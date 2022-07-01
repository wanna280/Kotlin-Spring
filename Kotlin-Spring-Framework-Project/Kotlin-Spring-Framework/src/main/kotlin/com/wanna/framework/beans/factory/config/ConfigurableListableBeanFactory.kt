package com.wanna.framework.beans.factory.config

import com.wanna.framework.beans.factory.ListableBeanFactory
import com.wanna.framework.beans.factory.support.AutowireCapableBeanFactory
import com.wanna.framework.beans.factory.support.DependencyDescriptor
import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.context.exception.NoSuchBeanDefinitionException

/**
 * 同时组合ConfigurableBeanFactory、ListableBeanFactory、AutowireCapableBeanFactory，并新增BeanDefinition的获取、依赖的解析等功能。
 */
interface ConfigurableListableBeanFactory : ListableBeanFactory, ConfigurableBeanFactory, AutowireCapableBeanFactory {

    /**
     * 获取BeanDefinition，一定能获取到，如果获取不到直接抛出异常；
     * 在获取之前，一定记得使用contains方法去进行检验，避免NoSuchBeanDefinitionException
     *
     * @param beanName beanName
     * @throws NoSuchBeanDefinitionException 如果没有找到这样的BeanDefinition异常
     */
    fun getBeanDefinition(beanName: String): BeanDefinition

    /**
     * 注册一个可以被解析的依赖，注册之后，后续可以通过Autowire进行获取；在进行元素的解析时，支持从注册的依赖当中去进行获取
     *
     * @param dependencyType 要去进行注册的依赖的类型
     * @param autowireValue 该依赖要使用的值？
     */
    fun registerResolvableDependency(dependencyType: Class<*>, autowireValue: Any)

    /**
     * 判断一个Bean是否是Autowire候选的Bean
     *
     * @param beanName requestingBeanName
     * @param descriptor 要去进行注入的元素的依赖描述符
     * @return 它支持是一个Autowire的CandidateBean？
     */
    fun isAutowireCandidate(beanName: String, descriptor: DependencyDescriptor): Boolean

    /**
     * 预实例化所有的单实例Bean(完成剩下所有单实例Bean的实例化和初始化)
     */
    fun preInstantiateSingletons()

    /**
     * destroy所有的单实例Bean
     */
    fun destroySingletons()
}