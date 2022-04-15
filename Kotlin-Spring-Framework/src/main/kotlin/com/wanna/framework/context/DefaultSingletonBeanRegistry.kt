package com.wanna.framework.context

import com.wanna.framework.context.exception.BeanCurrentlyInCreationException
import java.util.concurrent.ConcurrentHashMap

/**
 * 它是一个默认的单实例Bean的注册中心，维护了三级缓存，可以从三级缓存当中去获取Bean
 */
open class DefaultSingletonBeanRegistry : SingletonBeanRegistry {

    // 当前Bean是否正在创建当中？
    private val singletonsCurrentlyInCreation = HashSet<String>()

    // 一级缓存，维护了单实例的Bean的Map
    private val singletonObjects = ConcurrentHashMap<String, Any>()

    // 二级缓存，维护了早期的单实例Bean
    private val earlySingletonObjects = ConcurrentHashMap<String, Any>()

    // 三级缓存维护了ObjectFactory
    private val singletonFactories = HashMap<String, ObjectFactory<*>>()

    /**
     * 获取单实例的Bean
     *
     * @param beanName beanName
     * @param allowEarlyReference 是否允许早期引用？主要是解决循环依赖问题
     */
    open fun getSingleton(beanName: String, allowEarlyReference: Boolean): Any? {
        // 先从一级缓存当中拿
        var singletonObject = singletonObjects[beanName]

        // 如果一级缓存中没有，并且当前Bean已经正在创建当中了，那么说明有可能在二级缓存/三级缓存中
        if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
            singletonObject = earlySingletonObjects[beanName]

            // 如果二级缓存中没有，那么判断，是否允许了早期引用？如果允许的话，那么说明允许循环依赖，有可能在三级缓存当中
            if (singletonObject == null && allowEarlyReference) {

                // 加锁，避免这个过程中，别的线程往缓存当中加入了对象...
                synchronized(singletonObjects) {
                    // 重新从一级缓存中拿，因为别的线程在之前的过程中加入了对象呢...
                    singletonObject = singletonObjects[beanName]
                    if (singletonObject == null) {
                        // 尝试从二级缓存中拿
                        singletonObject = earlySingletonObjects[beanName]
                        if (singletonObject == null) {
                            // 尝试从三级缓存中拿
                            val objectFactory = singletonFactories[beanName]

                            // 如果三级缓存中没有，那么直接return null；如果三级缓存中有，那么从三级缓存当中去进行获取对象
                            objectFactory?.let {
                                // 从三级缓存当中去获取到对象
                                singletonObject = it.getObject()

                                // 从三级缓存中移除，将早期的Java对象转移到二级缓存当中，避免造成重复代理...
                                singletonFactories.remove(beanName)
                                earlySingletonObjects[beanName] = singletonObject as Any
                            }
                        }
                    }
                }
            }
        }
        return singletonObject
    }

    /**
     * 获取单实例Bean，需要从ObjectFactory当中去获取Bean
     */
    open fun getSingleton(beanName: String, factory: ObjectFactory<*>): Any? {
        // beforeSingletonCreation...
        beforeSingletonCreation(beanName)

        val singletonObject: Any?
        val newCreation: Boolean
        try {
            // 调用objectFactory.getObject获取Bean，一般ObjectFactory在这里会是createBean方法
            singletonObject = factory.getObject()
            newCreation = true
        } catch (ex: Exception) {
            throw ex
        } finally {
            // afterSingletonCreation
            afterSingletonCreation(beanName)
        }
        // 如果是新创建的，还需要将Bean加入到一级缓存的列表当中
        if (newCreation && singletonObject != null) {
            addSingleton(beanName, singletonObject as Any)
        }
        return singletonObject as Any?
    }

    override fun getSingleton(beanName: String): Any? {
        return getSingleton(beanName, true)
    }

    /**
     * 在创建Bean之前要执行的操作
     */
    open fun beforeSingletonCreation(beanName: String) {
        // 如果添加失败，说明之前已经添加过，那么抛出当前Bean已经正在创建中的异常...
        if (!singletonsCurrentlyInCreation.add(beanName)) {
            throw BeanCurrentlyInCreationException("[$beanName] is current in creation")
        }
    }

    /**
     * 在创建Bean之后要执行的操作
     */
    open fun afterSingletonCreation(beanName: String) {
        // 如果移除失败，说明之前都没有添加过这个Bean，抛出不合法的状态异常
        if (!singletonsCurrentlyInCreation.remove(beanName)) {
            throw IllegalStateException("remove bean [$beanName] failed")
        }
    }

    /**
     * 添加SingletonFactory
     */
    open fun addSingletonFactory(beanName: String, factory: ObjectFactory<*>) {
        synchronized(singletonObjects) {
            if (!singletonObjects.contains(beanName)) {
                this.earlySingletonObjects.remove(beanName)
                this.singletonFactories[beanName] = factory
            }
        }
    }

    /**
     * 添加一个单实例的Bean
     */
    open fun addSingleton(beanName: String, singleton: Any) {
        synchronized(singletonObjects) {
            this.singletonObjects[beanName] = singleton
            this.earlySingletonObjects.remove(beanName)
            this.singletonFactories.remove(beanName)
        }
    }

    /**
     * 当前Bean是否正在创建中？
     */
    open fun isSingletonCurrentlyInCreation(beanName: String): Boolean {
        return singletonsCurrentlyInCreation.contains(beanName)
    }

    /**
     * 摧毁一个单例Bean，同时从三个缓存当中去移除
     */
    open fun destorySingleton(beanName: String) {
        synchronized(singletonObjects) {
            this.singletonObjects.remove(beanName)
            this.earlySingletonObjects.remove(beanName)
            this.singletonFactories.remove(beanName)
        }
    }

    /**
     * 注册一个单实例Bean到SingletonBeanRegistry
     */
    override fun registerSingleton(beanName: String, singleton: Any) {
        synchronized(singletonObjects) {
            // 从以及缓存当中拿到旧的Object实例，如果已经存在有旧的Object，那么抛出不合法的状态异常
            val oldObject = singletonObjects.get(beanName)
            if (oldObject != null) {
                throw IllegalStateException("在SingletonBeanRegistry中已经存在有[beanName=$beanName]的Bean")
            }
            // 将单实例Bean进行注册
            addSingleton(beanName, singleton)
        }
    }

    /**
     * 容器当中是否含有该beanName的Bean？
     */
    override fun containsSingleton(beanName: String): Boolean {
        return this.singletonObjects.contains(beanName)
    }

    /**
     * 获取要操作单实例Bean的锁
     */
    override fun getSingletonMutex(): Any {
        return singletonObjects
    }

    override fun getSingletonCount(): Int {
        return singletonObjects.size
    }

    override fun getSingletonNames(): Array<String> {
        return singletonObjects.keys.toTypedArray()
    }
}