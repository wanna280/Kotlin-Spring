package com.wanna.framework.context.support

import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.AutowireCapableBeanFactory
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.DefaultListableBeanFactory
import com.wanna.framework.context.ApplicationContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 这是一个通用的ApplicationContext，它组合了BeanFactory，为AbstractApplicationContext当中的相关方法提供了实现；
 * 子类当中只要继续根据此类去扩展自己相关的功能(比如注册配置类)，即可实现出一个比较完整的的ApplicationContext
 *
 * @see AbstractApplicationContext
 */
abstract class GenericApplicationContext(private val beanFactory: DefaultListableBeanFactory) :
    AbstractApplicationContext(), BeanDefinitionRegistry {

    // 提供一个无参数的副构造器，创建一个默认的BeanFactory
    constructor() : this(DefaultListableBeanFactory())

    // 容器是否已经刷新过？容器不允许被重复刷新
    private val refreshed = AtomicBoolean(false)

    override fun refreshBeanFactory() {
        if (!refreshed.compareAndSet(false, true)) {
            throw IllegalStateException("BeanFactory不能被重复刷新")
        }
    }

    open fun setAllowCircularReferences(allowCircularReferences: Boolean) {
        beanFactory.allowCircularReferences = allowCircularReferences
    }

    override fun getBeanFactory(): DefaultListableBeanFactory = beanFactory
    open fun isAllowCircularReferences() = beanFactory.allowCircularReferences
    override fun getBeanDefinitionCounts() = beanFactory.getBeanDefinitionCounts()
    override fun getBeanDefinitionNames() = beanFactory.getBeanDefinitionNames()
    override fun getBeanDefinitions() = beanFactory.getBeanDefinitions()
    override fun getBeanDefinition(beanName: String) = beanFactory.getBeanDefinition(beanName)
    override fun containsBeanDefinition(name: String) = beanFactory.containsBeanDefinition(name)
    override fun getAutowireCapableBeanFactory() = beanFactory
    override fun getBeanDefinitionCount() = beanFactory.getBeanDefinitionCount()
    override fun removeBeanDefinition(name: String) = beanFactory.removeBeanDefinition(name)
    override fun registerBeanDefinition(name: String, beanDefinition: BeanDefinition) =
        beanFactory.registerBeanDefinition(name, beanDefinition)

}