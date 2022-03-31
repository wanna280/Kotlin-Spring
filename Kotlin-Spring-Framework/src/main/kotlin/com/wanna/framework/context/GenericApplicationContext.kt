package com.wanna.framework.context

/**
 * 这是一个通用的ApplicationContext
 */
abstract class GenericApplicationContext(_beanFactory: DefaultListableBeanFactory?) : AbstractApplicationContext() {

    // 提供一个无参数的副构造器，支持先不进行BeanFactory的创建˚
    constructor() : this(null)

    // 这是维护的BeanFactory，保存了容器中的全部Bean信息
    protected var beanFactory: DefaultListableBeanFactory? = _beanFactory

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

    override fun getBeanNamesForType(type: Class<*>): List<String> {
        return beanFactory?.getBeanNamesForType(type) ?: ArrayList()
    }

    override fun <T> getBeansForType(type: Class<T>): List<T> {
        return beanFactory?.getBeansForType(type) ?: ArrayList()
    }

    override fun getBeanNamesForTypeIncludingAncestors(type: Class<*>): List<String> {
        return beanFactory?.getBeanNamesForTypeIncludingAncestors(type) ?: ArrayList()
    }

    override fun <T> getBeansForTypeIncludingAncestors(type: Class<T>): List<T> {
        return beanFactory?.getBeansForTypeIncludingAncestors(type) ?: ArrayList()
    }

    override fun getBeanDefinitionCounts(): Int {
        return beanFactory?.getBeanDefinitionCounts() ?: -1
    }

    override fun getBeanDefinitionNames(): List<String> {
        return beanFactory?.getBeanDefinitionNames() ?: ArrayList()
    }


}