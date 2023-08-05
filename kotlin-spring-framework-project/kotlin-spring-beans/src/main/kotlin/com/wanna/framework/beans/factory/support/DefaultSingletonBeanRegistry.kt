package com.wanna.framework.beans.factory.support

import com.wanna.framework.beans.factory.BeanCurrentlyInCreationException
import com.wanna.framework.beans.factory.ObjectFactory
import com.wanna.framework.beans.factory.config.SingletonBeanRegistry
import com.wanna.framework.lang.Nullable
import com.wanna.common.logging.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 它是一个默认的单实例Bean的注册中心, 维护了SpringBeanFactory的三级缓存, 可以从三级缓存当中去获取Bean
 *
 * @see SingletonBeanRegistry
 */
open class DefaultSingletonBeanRegistry : SingletonBeanRegistry {

    companion object {
        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(DefaultSingletonBeanRegistry::class.java)
    }

    /**
     * 标识当前已经正在销毁Bean了
     */
    private var singletonsCurrentlyInDestruction = false

    /**
     * 当前Bean是否正在创建当中? 
     */
    private val singletonsCurrentlyInCreation = Collections.newSetFromMap<String>(ConcurrentHashMap(16))

    /**
     * 一级缓存, 维护了单实例的Bean的Map
     */
    private val singletonObjects = ConcurrentHashMap<String, Any>(256)

    /**
     * 二级缓存, 维护了早期的单实例Bean
     */
    private val earlySingletonObjects = ConcurrentHashMap<String, Any>(16)

    /**
     * 三级缓存, 维护了ObjectFactory(创建Bean的Callback)
     */
    private val singletonFactories = HashMap<String, ObjectFactory<*>>(16)

    /**
     * 用来检查去进行应该排除的beanName
     */
    private val inCreationCheckExclusions = Collections.newSetFromMap<String>(ConcurrentHashMap(64))

    /**
     *  已经注册到SingletonBeanRegistry当中的singletonBean的列表, 对它的所有操作, 都需要使用singletonObjects锁
     *
     *  @see singletonObjects
     */
    private val registeredSingletons = LinkedHashSet<String>()

    /**
     * 注册了destroy的回调的Bean, 交给SingletonBeanRegistry统一管理(使用LinkedHashMap保证顺序);
     * 当发生destroy时, 需要将这些Bean去进行全部的destroy
     */
    private val disposableBeans = LinkedHashMap<String, DisposableBean>()

    /**
     * 根据beanName去获取单实例的Bean
     *
     * @param beanName beanName
     * @param allowEarlyReference 是否允许早期引用? 主要是解决循环依赖问题
     * @return 根据beanName获取到的单例Bean
     */
    open fun getSingleton(beanName: String, allowEarlyReference: Boolean): Any? {
        // 先从一级缓存当中拿
        var singletonObject = singletonObjects[beanName]

        // 如果一级缓存中没有, 并且当前Bean已经正在创建当中了, 那么说明有可能在二级缓存/三级缓存中
        if (singletonObject == null && isSingletonCurrentlyInCreation(beanName)) {
            singletonObject = earlySingletonObjects[beanName]

            // 如果二级缓存中没有, 那么判断, 是否允许了早期引用? 如果允许的话, 那么说明允许循环依赖, 有可能在三级缓存当中
            if (singletonObject == null && allowEarlyReference) {

                // 加锁, 避免这个过程中, 别的线程往缓存当中加入了对象...
                synchronized(singletonObjects) {
                    // 重新从一级缓存中拿, 因为别的线程在之前的过程中加入了对象呢...
                    singletonObject = singletonObjects[beanName]
                    if (singletonObject == null) {
                        // 尝试从二级缓存中拿
                        singletonObject = earlySingletonObjects[beanName]
                        if (singletonObject == null) {
                            // 尝试从三级缓存中拿
                            val objectFactory = singletonFactories[beanName]

                            // 如果三级缓存中没有, 那么直接return null; 如果三级缓存中有, 那么从三级缓存当中去进行获取对象
                            objectFactory?.let {
                                // 从三级缓存当中去获取到对象
                                singletonObject = it.getObject()

                                // 从三级缓存中移除, 将早期的Java对象转移到二级缓存当中, 避免造成重复代理...
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
     * 获取单实例Bean, 需要从ObjectFactory当中去获取Bean
     *
     * @param beanName beanName
     * @param factory ObjectFactory(call back)
     * @return 获取到的单例对象(获取不到return null)
     */
    open fun getSingleton(beanName: String, factory: ObjectFactory<*>): Any? {
        // beforeSingletonCreation...
        beforeSingletonCreation(beanName)

        val singletonObject: Any?
        val newCreation: Boolean
        try {
            // 调用objectFactory.getObject获取Bean, 一般ObjectFactory在这里会是createBean方法
            singletonObject = factory.getObject()
            newCreation = true
        } catch (ex: Exception) {
            throw ex
        } finally {
            // afterSingletonCreation...
            afterSingletonCreation(beanName)
        }
        // 如果是新创建的, 还需要将Bean加入到一级缓存的列表当中
        if (newCreation && singletonObject != null) {
            addSingleton(beanName, singletonObject)
        }
        return singletonObject
    }

    /**
     * 获取单实例Bean, 对外提供的方法
     *
     * @param beanName beanName
     * @return 获取到的SingletonBean, 有可能为null
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
        // 如果添加失败, 说明之前已经添加过, 那么抛出当前Bean已经正在创建中的异常...
        if (!singletonsCurrentlyInCreation.add(beanName)) {
            throw BeanCurrentlyInCreationException(
                "[$beanName]正在创建当中, 当前正在创建的有[$singletonsCurrentlyInCreation]", null, beanName
            )
        }
    }

    /**
     * 在创建Bean之后要执行的操作
     *
     * @param beanName beanName
     */
    open fun afterSingletonCreation(beanName: String) {
        // 如果移除失败, 说明之前都没有添加过这个Bean, 抛出不合法的状态异常
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
     * 当前Bean是否正在创建中? 
     *
     * @param beanName 要判断的beanName
     */
    open fun isSingletonCurrentlyInCreation(beanName: String): Boolean {
        return singletonsCurrentlyInCreation.contains(beanName)
    }

    /**
     * 设置某个Bean是否正在创建当中的状态
     *
     * @param beanName beanName
     * @param inCreation 是否正在创建当中? 如果为true代表添加、为false代表删除
     */
    open fun setCurrentlyInCreation(beanName: String, inCreation: Boolean) {
        if (!inCreation) {
            this.inCreationCheckExclusions -= beanName
        } else {
            this.inCreationCheckExclusions += beanName
        }
    }

    /**
     * 判断给定的beanNam的Bean是否正在创建当中了? 
     *
     * @param beanName beanName
     * @return 如果当前正在创建当中, 那么return true; 否则return false
     */
    open fun isCurrentlyInCreation(beanName: String): Boolean {
        return this.inCreationCheckExclusions.contains(beanName) || this.isSingletonCurrentlyInCreation(beanName)
    }

    /**
     * 移除一个Singleton单实例Bean, 同时尝试从三级缓存当中移除(within singleton lock)
     *
     * @param beanName beanName
     */
    protected open fun removeSingleton(beanName: String) {
        synchronized(singletonObjects) {
            this.singletonObjects.remove(beanName)
            this.earlySingletonObjects.remove(beanName)
            this.singletonFactories.remove(beanName)
            this.registeredSingletons.remove(beanName)
        }
    }

    /**
     * 从BeanDefinitionRegistry当中去摧毁一个单例Bean,
     * 同时从三个缓存当中去移除, 并回调DisposableBean
     *
     * @param beanName beanName
     */
    open fun destroySingleton(beanName: String) {
        // 1.从三级缓存当中去移除单实例Bean
        removeSingleton(beanName)

        // 2.从DisposableBeans列表当中去移除一个DisposableBean,
        // 如果之前已经存在, 那么会将移除的DisposableBean去进行return...
        val disposableBean: DisposableBean?
        synchronized(this.disposableBeans) {
            disposableBean = this.disposableBeans.remove(beanName)
        }

        // 3.如果必要的话, 去回调它(DisposableBean)的destroy方法
        destroyBean(beanName, disposableBean)
    }

    /**
     * 摧毁一个Bean, 回调它的destory方法, 并完成相关的后续处理工作
     *
     * @param beanName beanName
     * @param disposableBean disposableBean(有可能为null)
     */
    protected open fun destroyBean(beanName: String, @Nullable disposableBean: DisposableBean?) {
        if (disposableBean != null) {
            try {
                disposableBean.destroy()
            } catch (ex: Throwable) {
                if (logger.isDebugEnabled) {
                    logger.debug("执行DisposableBean[beanName=$beanName]的destory方法失败, 原因是[$ex]")
                }
            }
        }
    }

    /**
     * 注册一个DisposableBean到单实例Bean的注册中心(SingletonBeanRegistry)当中
     *
     * @param bean 需要去进行注册的DisposableBean
     * @param name DisposalBean的beanName
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
     * @param singleton 单例Bean对象
     * @throws IllegalStateException 如果已经注册过该name的单例Bean(不能重复注册)
     */
    override fun registerSingleton(beanName: String, singleton: Any) {
        synchronized(singletonObjects) {
            // 从以及缓存当中拿到旧的Object实例, 如果已经存在有旧的Object, 那么抛出不合法的状态异常
            val oldObject = singletonObjects[beanName]
            if (oldObject != null) {
                throw IllegalStateException("在SingletonBeanRegistry中已经存在有[beanName=$beanName]的Bean")
            }
            // 将单实例Bean进行注册
            addSingleton(beanName, singleton)
        }
    }

    /**
     * 容器当中是否含有该beanName的Bean? 只检查SingletonObjects缓存
     *
     * @param beanName beanName
     * @return 如果SingletonObjects当中包含了该对象, 那么return true; 否则return false
     */
    override fun containsSingleton(beanName: String): Boolean {
        return this.singletonObjects.containsKey(beanName)  // fixed:contains-->containsKey
    }

    /**
     * 获取要操作单实例Bean的锁
     *
     * @return 单例对象锁
     */
    override fun getSingletonMutex() = singletonObjects

    /**
     * 获取单实例Bean的注册中心当中的单实例Bean的数量
     *
     * @return 所有的已经注册的单例Bean的数量
     */
    override fun getSingletonCount(): Int {
        synchronized(this.singletonObjects) {
            return this.registeredSingletons.size
        }
    }

    /**
     * 获取单实例Bean的beanName列表
     *
     * @return 所有已经注册的单例Bean的beanName
     */
    override fun getSingletonNames(): Array<String> {
        synchronized(this.singletonObjects) {
            return this.registeredSingletons.toTypedArray()
        }
    }

    /**
     * clear掉当前的SingletonBeanRegistry当中的所有的已经注册的SingletonBean的缓存
     */
    protected open fun clearSingletonCache() {
        synchronized(this.singletonObjects) {
            this.singletonObjects.clear()
            this.earlySingletonObjects.clear()
            this.singletonFactories.clear()
            this.registeredSingletons.clear()
            this.singletonsCurrentlyInDestruction = false
        }
    }

    /**
     * 摧毁当前的SingleBeanRegistry所有的单实例Bean
     */
    open fun destroySingletons() {
        // set inDestruction to true
        synchronized(this.singletonObjects) {
            this.singletonsCurrentlyInDestruction = true
        }

        // 逆序回调所有的DisposableBean, 去完成Bean的摧毁工作
        this.disposableBeans.keys.reversed().forEach(this::destroySingleton)

        // clear singleton Cache & set inDestruction to false
        this.clearSingletonCache()
    }
}