package com.wanna.framework.context.support

import com.wanna.framework.beans.factory.support.definition.BeanDefinition
import com.wanna.framework.beans.factory.support.AutowireCapableBeanFactory
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.support.DefaultListableBeanFactory
import com.wanna.framework.context.ApplicationContext
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 这是一个通用的ApplicationContext
 */
abstract class GenericApplicationContext(_beanFactory: DefaultListableBeanFactory) : AbstractApplicationContext(),
    BeanDefinitionRegistry {

    // 提供一个无参数的副构造器，创建一个默认的BeanFactory
    constructor() : this(DefaultListableBeanFactory())

    // 容器是否已经刷新过？
    private val refreshed = AtomicBoolean(false)



    // 这是维护的BeanFactory，保存了容器中的全部Bean信息
    private var beanFactory: DefaultListableBeanFactory = _beanFactory

    open fun setAllowCircularReferences(allowCircularReferences: Boolean) {
        beanFactory.allowCircularReferences = allowCircularReferences
    }

    open fun isAllowCircularReferences(): Boolean {
        return beanFactory.allowCircularReferences
    }

    override fun refreshBeanFactory() {
        if (!refreshed.compareAndSet(false, true)) {
            throw IllegalStateException("BeanFactory不能被重复刷新")
        }
    }

    override fun getBeanFactory(): DefaultListableBeanFactory {
        return beanFactory
    }

    override fun getBeanNamesForType(type: Class<*>): List<String> {
        return beanFactory.getBeanNamesForType(type) ?: ArrayList()
    }

    override fun <T> getBeansForType(type: Class<T>): Map<String, T> {
        return beanFactory.getBeansForType(type) ?: HashMap()
    }

    override fun getBeanNamesForTypeIncludingAncestors(type: Class<*>): List<String> {
        return beanFactory.getBeanNamesForTypeIncludingAncestors(type) ?: ArrayList()
    }

    override fun <T> getBeansForTypeIncludingAncestors(type: Class<T>): Map<String, T> {
        return beanFactory.getBeansForTypeIncludingAncestors(type) ?: HashMap()
    }

    override fun getBeanDefinitionCounts(): Int {
        return beanFactory.getBeanDefinitionCounts() ?: -1
    }

    override fun getBeanDefinitionNames(): List<String> {
        return beanFactory.getBeanDefinitionNames() ?: ArrayList()
    }

    override fun registerBeanDefinition(name: String, beanDefinition: BeanDefinition) {
        beanFactory.registerBeanDefinition(name, beanDefinition)
    }

    override fun getBeanDefinitions(): List<BeanDefinition> {
        return beanFactory.getBeanDefinitions() ?: ArrayList()
    }

    override fun getBeanDefinition(beanName: String): BeanDefinition? {
        return beanFactory.getBeanDefinition(beanName)
    }

    override fun containsBeanDefinition(name: String): Boolean {
        return beanFactory.containsBeanDefinition(name) ?: false
    }

    override fun getAutowireCapableBeanFactory(): AutowireCapableBeanFactory {
        return beanFactory
    }

    override fun getBeanDefinitionCount(): Int {
        return beanFactory.getBeanDefinitionCount()
    }
}