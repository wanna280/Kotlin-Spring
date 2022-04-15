package com.wanna.framework.context

import com.wanna.framework.beans.factory.support.definition.BeanDefinition

/**
 * 这是一个通用的ApplicationContext
 */
abstract class GenericApplicationContext(_beanFactory: DefaultListableBeanFactory?) : AbstractApplicationContext(),
    BeanDefinitionRegistry {

    // 提供一个无参数的副构造器，支持先不进行BeanFactory的创建˚
    constructor() : this(null)

    // 这是维护的BeanFactory，保存了容器中的全部Bean信息
    private var beanFactory: DefaultListableBeanFactory? = _beanFactory

    fun setAllowCircularReferences(allowCircularReferences: Boolean) {
        beanFactory!!.allowCircularReferences = allowCircularReferences
    }

    fun isAllowCircularReferences(): Boolean {
        return beanFactory!!.allowCircularReferences
    }

    /**
     * 获取BeanFactory，如果它还没被创建，那么在这里去完成创建，并进行return
     */
    override fun obtainBeanFactory(): ConfigurableListableBeanFactory {
        // 如果之前没有完成过初始化，那么需要完成BeanFactory的初始化
        if (beanFactory == null) {
            beanFactory = DefaultListableBeanFactory()
        }
        return beanFactory as DefaultListableBeanFactory
    }

    override fun getBeanFactory(): DefaultListableBeanFactory {
        return beanFactory!!
    }

    override fun getBeanNamesForType(type: Class<*>): List<String> {
        return beanFactory?.getBeanNamesForType(type) ?: ArrayList()
    }

    override fun <T> getBeansForType(type: Class<T>): Map<String, T> {
        return beanFactory?.getBeansForType(type) ?: HashMap()
    }

    override fun getBeanNamesForTypeIncludingAncestors(type: Class<*>): List<String> {
        return beanFactory?.getBeanNamesForTypeIncludingAncestors(type) ?: ArrayList()
    }

    override fun <T> getBeansForTypeIncludingAncestors(type: Class<T>): Map<String, T> {
        return beanFactory?.getBeansForTypeIncludingAncestors(type) ?: HashMap()
    }

    override fun getBeanDefinitionCounts(): Int {
        return beanFactory?.getBeanDefinitionCounts() ?: -1
    }

    override fun getBeanDefinitionNames(): List<String> {
        return beanFactory?.getBeanDefinitionNames() ?: ArrayList()
    }

    override fun registerBeanDefinition(name: String, beanDefinition: BeanDefinition) {
        beanFactory?.registerBeanDefinition(name, beanDefinition)
    }

    override fun getBeanDefinitions(): List<BeanDefinition> {
        return beanFactory?.getBeanDefinitions() ?: ArrayList()
    }

    override fun getBeanDefinition(beanName: String): BeanDefinition? {
        return beanFactory?.getBeanDefinition(beanName)
    }

    override fun containsBeanDefinition(beanName: String): Boolean {
        return beanFactory?.containsBeanDefinition(beanName) ?: false
    }

    override fun getAutowireCapableBeanFactory(): AutowireCapableBeanFactory {
        return beanFactory as AutowireCapableBeanFactory
    }
}