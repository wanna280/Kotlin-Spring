package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.factory.support.definition.RootBeanDefinition
import com.wanna.framework.beans.factory.BeanFactory
import java.lang.reflect.Constructor
import java.lang.reflect.Method

/**
 * 这是Spring当中对于一个Bean的实例化策略，提供三种实例化方式，分别是从BeanDefinition当中解析/给定构造器/给定FactoryBeanMethod
 */
interface InstantiationStrategy {
    /**
     * 实例化方式1：只给BeanDefinition，那么需要从BeanDefinition当中去解析合适的Constructor去进行实例
     *
     * @param bd MergedBeanDefinition
     * @param beanName beanName
     * @param owner beanFactory
     * @return 创建完成的对象
     */
    fun instantiate(bd: RootBeanDefinition, beanName: String?, owner: BeanFactory): Any

    /**
     * 实例化方式2：给了BeanDefinition，还给了构造器，直接通过构造器去完成Bean的实例化
     *
     * @param bd MergedBeanDefinition
     * @param beanName beanName
     * @param owner beanFactory
     * @param ctor 实例化选用的构造器
     * @param args 构造器的参数列表
     * @return 创建完成的对象
     */
    fun instantiate(
        bd: RootBeanDefinition, beanName: String?, owner: BeanFactory, ctor: Constructor<*>, vararg args: Any?
    ): Any

    /**
     * 实例化方式3：给了BeanDefinition，还给了factoryMethod和factoryBean，需要通过工厂(@Bean)方法去完成对象的实例化
     *
     * @param bd MergedBeanDefinition
     * @param beanName beanName
     * @param owner beanFactory
     * @param factoryBean FactoryMethod所在的Bean
     * @param factoryMethod 工厂方法(@Bean方法)
     * @param args 工厂方法的参数
     * @return 创建完成的对象
     */
    fun instantiate(
        bd: RootBeanDefinition,
        beanName: String?,
        owner: BeanFactory,
        factoryMethod: Method,
        factoryBean: Any,
        vararg args: Any?
    ): Any?
}