package com.wanna.framework.context

/**
 * 它是一个FactoryBean的注册中心
 */
open class FactoryBeanRegistrySupport() : DefaultSingletonBeanRegistry() {

    // FactoryBeanObject的缓存列表
    private val factoryBeanObjectCache = LinkedHashMap<String, Any>()

    /**
     * 从FactoryBeanObject当中去创建Bean
     */
    fun getFactoryBeanForObject(beanName: String): Any? {
        return factoryBeanObjectCache[beanName]
    }

    /**
     * 从FactoryBean当中去获取Object
     */
    fun getObjectFromFactoryBean(factoryBean: FactoryBean<*>, beanName: String, shouldProcess: Boolean): Any? {
        if (containsSingleton(beanName) && factoryBean.isSingleton()) {
            synchronized(getSingletonLock()) {
                var instance = factoryBeanObjectCache[beanName]
                if (instance == null) {
                    instance = doGetObjectFromFactoryBean(factoryBean)
                    factoryBeanObjectCache[beanName] = instance as Any
                }
                return instance as Any?
            }
        } else {

            return Any()
        }
    }

    /**
     * 容器当中是否含有该beanName的Bean？
     */
    fun containsSingleton(beanName: String): Boolean {
        return getSingleton(beanName, true) != null
    }

    fun doGetObjectFromFactoryBean(factoryBean: FactoryBean<*>): Any? {
        try {
            return factoryBean.getObject() as Any
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }
}