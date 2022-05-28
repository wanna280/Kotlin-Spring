package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.factory.ObjectFactory
import com.wanna.framework.beans.factory.config.SingletonBeanRegistry
import com.wanna.framework.context.exception.BeanCurrentlyInCreationException
import org.slf4j.LoggerFactory
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap

/**
 * 它是一个默认的单实例Bean的注册中心，维护了SpringBeanFactory的三级缓存，可以从三级缓存当中去获取Bean
 */
open class DefaultSingletonBeanRegistry : SingletonBeanRegistry {

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultSingletonBeanRegistry::class.java)
    }

    // 当前Bean是否正在创建当中？
    private val singletonsCurrentlyInCreation = Collections.newSetFromMap<String>(ConcurrentHashMap(16))

    // 一级缓存，维护了单实例的Bean的Map
    private val singletonObjects = ConcurrentHashMap<String, Any>(256)

    // 二级缓存，维护了早期的单实例Bean
    private val earlySingletonObjects = ConcurrentHashMap<String, Any>(16)

    // 三级缓存维护了ObjectFactory
    private val singletonFactories = HashMap<String, ObjectFactory<*>>(16)

    // 已经注册到SingletonBeanRegistry当中的singletonBean的列表，对它的所有操作，都需要使用singletonObjects锁
    private val registeredSingletons = LinkedHashSet<String>()

    // 注册了destroy的回调的Bean，交给SingletonBeanRegistry统一管理
    private val disposableBeans = LinkedHashMap<String, DisposableBean>()

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
     *
     * @param beanName beanName
     * @param factory ObjectFactory(call back)
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
            addSingleton(beanName, singletonObject)
        }
        return singletonObject
    }

    /**
     * 获取单实例Bean，对外提供的方法
     *
     * @param beanName beanName
     * @return 获取到的SingletonBean，有可能为null
     */
    override fun getSingleton(beanName: String): Any? {
        return getSingleton(beanName, true)
    }

    /**
     * 在创建Bean之前要执行的操作
     *
     * @param beanName beanName
     */
    open fun beforeSingletonCreation(beanName: String) {
        // 如果添加失败，说明之前已经添加过，那么抛出当前Bean已经正在创建中的异常...
        if (!singletonsCurrentlyInCreation.add(beanName)) {
            throw BeanCurrentlyInCreationException("[$beanName] is current in creation")
        }
    }

    /**
     * 在创建Bean之后要执行的操作
     *
     * @param beanName beanName
     */
    open fun afterSingletonCreation(beanName: String) {
        // 如果移除失败，说明之前都没有添加过这个Bean，抛出不合法的状态异常
        if (!singletonsCurrentlyInCreation.remove(beanName)) {
            throw IllegalStateException("remove bean [$beanName] failed")
        }
    }

    /**
     * 添加SingletonFactory到singletonFactories当中
     *
     * @param beanName beanName
     * @param factory ObjectFactory对象
     */
    open fun addSingletonFactory(beanName: String, factory: ObjectFactory<*>) {
        synchronized(singletonObjects) {
            if (!singletonObjects.contains(beanName)) {
                this.earlySingletonObjects -= beanName
                this.singletonFactories[beanName] = factory
            }
        }
    }

    /**
     * 添加一个单实例的Bean到singletonObjects当中
     *
     * @param beanName beanName
     * @param singleton 单例对象
     */
    open fun addSingleton(beanName: String, singleton: Any) {
        synchronized(singletonObjects) {
            this.singletonObjects[beanName] = singleton
            this.earlySingletonObjects -= beanName
            this.singletonFactories -= beanName
            this.registeredSingletons += beanName
        }
    }

    /**
     * 当前Bean是否正在创建中？
     *
     * @param beanName 要判断的beanName
     */
    open fun isSingletonCurrentlyInCreation(beanName: String): Boolean {
        return singletonsCurrentlyInCreation.contains(beanName)
    }

    /**
     * 移除一个Singleton单实例Bean，同时尝试从三级缓存当中移除(within lock)
     *
     * @param beanName beanName
     */
    protected open fun removeSingleton(beanName: String) {
        synchronized(singletonObjects) {
            this.singletonObjects -= beanName
            this.earlySingletonObjects -= beanName
            this.singletonFactories -= beanName
            this.registeredSingletons -= beanName
        }
    }

    /**
     * 从BeanDefinitionRegistry当中去摧毁一个单例Bean，同时从三个缓存当中去移除，并回调DisposableBean
     *
     * @param beanName beanName
     */
    open fun destroySingleton(beanName: String) {
        // 1.从三级缓存当中去移除单实例Bean
        removeSingleton(beanName)

        // 2.从DisposableBeans列表当中去移除一个DisposableBean
        val disposableBean: DisposableBean?
        synchronized(this.disposableBeans) {
            disposableBean = this.disposableBeans.remove(beanName)
        }

        // 3.如果必要的话，去回调它的destroy方法
        destoryBean(beanName, disposableBean)
    }

    /**
     * 摧毁一个Bean，回调它的destory方法，并完成相关的后续处理工作
     *
     * @param beanName beanName
     * @param disposableBean disposableBean(有可能为null)
     */
    protected open fun destoryBean(beanName: String, disposableBean: DisposableBean?) {
        if (disposableBean != null) {
            try {
                disposableBean.destroy()
            } catch (ex: Throwable) {
                if (logger.isDebugEnabled) {
                    logger.debug("执行DisposableBean[beanName=$beanName]的destory方法失败，原因是[$ex]")
                }
            }
        }
    }

    /**
     * 注册一个DisposableBean到单实例Bean的注册中心(SingletonBeanRegistry)当中
     *
     * @param bean DisposableBean
     * @param name beanName
     */
    protected open fun registerDisposableBean(name: String, bean: DisposableBean) {
        synchronized(this.disposableBeans) {
            this.disposableBeans[name] = bean
        }
    }

    /**
     * 注册一个单实例Bean到SingletonBeanRegistry
     *
     * @param beanName beanName
     * @param singleton 单例Bean
     * @throws IllegalStateException 如果已经注册过该name的单例Bean(不能重复注册)
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
     * 容器当中是否含有该beanName的Bean？只检查SingletonObjects缓存
     *
     * @param beanName beanName
     */
    override fun containsSingleton(beanName: String): Boolean {
        return this.singletonObjects.contains(beanName)
    }

    /**
     * 获取要操作单实例Bean的锁
     */
    override fun getSingletonMutex(): Any = singletonObjects

    /**
     * 获取单实例Bean的注册中心当中的单实例Bean的数量
     */
    override fun getSingletonCount(): Int {
        synchronized(this.singletonObjects) {
            return this.registeredSingletons.size
        }
    }

    /**
     * 获取单实例Bean的beanName列表
     */
    override fun getSingletonNames(): Array<String> {
        synchronized(this.singletonObjects) {
            return this.registeredSingletons.toTypedArray()
        }
    }
}