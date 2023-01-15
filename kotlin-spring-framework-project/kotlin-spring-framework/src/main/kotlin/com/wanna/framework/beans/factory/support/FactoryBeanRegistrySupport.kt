package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.factory.FactoryBean

/**
 * 它是一个FactoryBean的注册中心, 在DefaultSingletonBeanRegistry的基础上, 新增了FactoryBean的缓存的支持和管理...
 * 为BeanFactory的实现, 提供了很多相关的功能上的支持
 *
 * @see DefaultSingletonBeanRegistry
 * @see AbstractBeanFactory
 * @see DefaultListableBeanFactory
 */
open class FactoryBeanRegistrySupport : DefaultSingletonBeanRegistry() {

    /**
     * FactoryBeanObject的缓存列表
     */
    private val factoryBeanObjectCache = LinkedHashMap<String, Any>()

    /**
     * 根据beanName从FactoryBeanObject当中去获取FactoryBeanObject
     *
     * @param beanName beanName
     * @return FactoryBeanObject(如果没有的话, return null)
     */
    open fun getCachedFactoryBeanForObject(beanName: String): Any? = factoryBeanObjectCache[beanName]

    /**
     * 从FactoryBean当中去获取FactoryBeanObject, 通过调用FactoryBean.getObject方法, 去获取到该FactoryBean要导入的FactoryBeanObject
     *
     * @param factoryBean FactoryBean
     * @param beanName beanName(不包含"&"的beanName)
     * @param shouldProcess 是否应该被后置处理？如果应该的话, 那么会使用BeanPostProcessor去进行后置处理(完成代理工作)
     */
    open fun getObjectFromFactoryBean(factoryBean: FactoryBean<*>, beanName: String, shouldProcess: Boolean): Any {
        // 1.如果该FactoryBean是单例的, 并且已经包含了该SingletonBean的话...
        if (containsSingleton(beanName) && factoryBean.isSingleton()) {
            synchronized(getSingletonMutex()) {
                // 尝试从缓存去进行获取, 如果获取不到, 那么就去构建一波FactoryBeanObject
                var factoryBeanObject = factoryBeanObjectCache[beanName]
                if (factoryBeanObject == null) {
                    factoryBeanObject = doGetObjectFromFactoryBean(factoryBean)
                }
                // 如果应该被后置处理的话, 那么使用BeanPostProcessor去进行后置处理工作
                if (shouldProcess) {
                    beforeSingletonCreation(beanName)
                    factoryBeanObject = postProcessObjectFromFactoryBean(factoryBeanObject!!, beanName)
                    afterSingletonCreation(beanName)
                }
                if (containsSingleton(beanName)) {
                    factoryBeanObjectCache[beanName] = factoryBeanObject!!  // put Cache
                }
                return factoryBeanObject!!
            }

            // 如果还没有包含该Singleton, 或者它是Prototype的FactoryBean的话...
        } else {
            var factoryBeanObject = doGetObjectFromFactoryBean(factoryBean)
            if (shouldProcess) {
                factoryBeanObject = postProcessObjectFromFactoryBean(factoryBeanObject, beanName)
            }
            return factoryBeanObject
        }
    }

    /**
     * 对FactoryBean去完成后置处理, 模板方法, 交给子类去进行实现
     *
     * @param factoryBeanObject FactoryBeanObject
     * @param beanName beanName
     * @return 完成后置处理的FactoryBeanObject
     */
    protected open fun postProcessObjectFromFactoryBean(factoryBeanObject: Any, beanName: String): Any {
        return factoryBeanObject
    }

    /**
     * 从FactoryBean当中去获取到FactoryBeanObject
     *
     * @param factoryBean FactoryBean
     * @return FactoryBeanObject
     */
    open fun doGetObjectFromFactoryBean(factoryBean: FactoryBean<*>): Any {
        try {
            return factoryBean.getObject() as Any
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return NullBean()
    }

    /**
     * 获取FactoryBean的Object的Type
     *
     * @param factoryBean FactoryBean
     * @return FactoryBeanObjectType
     */
    open fun getTypeForFactoryBean(factoryBean: FactoryBean<*>): Class<*> {
        return factoryBean.getObjectType()
    }
}