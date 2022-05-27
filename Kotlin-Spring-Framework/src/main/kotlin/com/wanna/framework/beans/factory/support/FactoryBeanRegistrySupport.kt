package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.factory.FactoryBean

/**
 * 它是一个FactoryBean的注册中心，在DefaultSingletonBeanRegistry的基础上，新增了FactoryBean的缓存的支持和管理...
 */
open class FactoryBeanRegistrySupport : DefaultSingletonBeanRegistry() {

    // FactoryBeanObject的缓存列表
    private val factoryBeanObjectCache = LinkedHashMap<String, Any>()

    /**
     * 根据beanName从FactoryBeanObject当中去获取Bean
     *
     * @param beanName beanName
     */
    open fun getFactoryBeanForObject(beanName: String) = factoryBeanObjectCache[beanName]

    /**
     * 从FactoryBean当中去获取FactoryBeanObject
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