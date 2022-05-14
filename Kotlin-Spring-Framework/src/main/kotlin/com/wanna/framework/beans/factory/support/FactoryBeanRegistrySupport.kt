package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.factory.FactoryBean

/**
 * 它是一个FactoryBean的注册中心
 */
open class FactoryBeanRegistrySupport : DefaultSingletonBeanRegistry() {

    // FactoryBeanObject的缓存列表
    private val factoryBeanObjectCache = LinkedHashMap<String, Any>()

    /**
     * 从FactoryBeanObject当中去创建Bean
     */
    open fun getFactoryBeanForObject(beanName: String) = factoryBeanObjectCache[beanName]

    /**
     * 从FactoryBean当中去获取Object
     */
    open fun getObjectFromFactoryBean(factoryBean: FactoryBean<*>, beanName: String, shouldProcess: Boolean): Any? {
        if (containsSingleton(beanName) && factoryBean.isSingleton()) {
            synchronized(getSingletonMutex()) {
                var instance = factoryBeanObjectCache[beanName]
                if (instance == null) {
                    instance = doGetObjectFromFactoryBean(factoryBean)
                    factoryBeanObjectCache[beanName] = instance as Any
                }
                return instance
            }
        } else {

            return Any()
        }
    }

    open fun doGetObjectFromFactoryBean(factoryBean: FactoryBean<*>): Any? {
        try {
            return factoryBean.getObject() as Any
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

    /**
     * 获取FactoryBean的Object的Type
     */
    open fun getTypeForFactoryBean(factoryBean: FactoryBean<*>): Class<*>? {
        return factoryBean.getObjectType()
    }
}