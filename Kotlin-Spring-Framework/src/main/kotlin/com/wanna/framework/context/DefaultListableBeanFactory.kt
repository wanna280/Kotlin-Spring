package com.wanna.framework.context

import com.wanna.framework.beans.BeanDefinition

open class DefaultListableBeanFactory() : ConfigurableListableBeanFactory, AbstractAutowireCapableBeanFactory() {

    // beanDefinitionMap
    private val bdMap = HashMap<String, BeanDefinition>()

    // beanDefinitionNames
    private val bdNames = ArrayList<String>()

    override fun registerBeanDefinition(name: String, beanDefinition: BeanDefinition) {
        bdNames += (name)
        bdMap[name] = beanDefinition
    }

    override fun preInstantiateSingletons() {
        bdNames.forEach {
            getBean(it)
        }
    }

    override fun getBeanDefinition(beanName: String): BeanDefinition? {
        return bdMap[beanName]
    }

    override fun getBeanDefinitionNames(): List<String> {
        return ArrayList(bdNames)
    }

    override fun getBeanDefinitions(): List<BeanDefinition> {
        return ArrayList(bdMap.values)
    }

    override fun getBeanDefinitionCounts(): Int {
        return bdNames.size
    }
}