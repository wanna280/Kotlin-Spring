package com.wanna.framework.context

import com.wanna.framework.beans.factory.HierarchicalBeanFactory
import com.wanna.framework.beans.factory.ListableBeanFactory
import com.wanna.framework.beans.factory.support.AutowireCapableBeanFactory
import com.wanna.framework.context.event.ApplicationEventPublisher
import com.wanna.framework.core.environment.EnvironmentCapable
import com.wanna.framework.core.io.support.ResourcePatternResolver

/**
 * 这是一个Spring应用的上下文，它继承了BeanFactory，并拥有了BeanFactory中的所有功能；
 *
 * * 1.ApplicationEventPublisher，支持去进行事件的发布
 * * 2.ListableBeanFactory/HierarchicalBeanFactory，因为寻找Bean有可能需要使用到parentBeanFactory去的方式去进行寻找；
 * 但是有可能parentBeanFactory就是ApplicationContext...这里就需要保证ApplicationContext去提供ListableBeanFactory/HierarchicalBeanFactory功能才能实现
 * * 3.EnvironmentCapable，标识它支持去进行环境的获取(有些地方需要用到环境，可以使用这个接口去进行获取)
 */
interface ApplicationContext : ApplicationEventPublisher, ListableBeanFactory, EnvironmentCapable,
    HierarchicalBeanFactory,ResourcePatternResolver {

    /**
     * 获取当前的ApplicationContext的Id
     *
     * @return ApplicationContextId
     */
    fun getId() : String?

    /**
     * 获取可以自动装配的BeanFactory，可以用它完成Bean的创建/销毁等工作
     */
    fun getAutowireCapableBeanFactory(): AutowireCapableBeanFactory

    /**
     * 获取parent ApplicationContext，如果有的话；如果没有的话，return null
     */
    fun getParent(): ApplicationContext?

    /**
     * 获取BeanClassLoader
     */
    fun getBeanClassLoader(): ClassLoader
}