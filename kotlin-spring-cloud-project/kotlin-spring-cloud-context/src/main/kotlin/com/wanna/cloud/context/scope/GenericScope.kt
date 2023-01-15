package com.wanna.cloud.context.scope

import com.wanna.framework.beans.factory.ObjectFactory
import com.wanna.framework.beans.factory.config.BeanDefinitionRegistry
import com.wanna.framework.beans.factory.config.ConfigurableListableBeanFactory
import com.wanna.framework.beans.factory.config.Scope
import com.wanna.framework.beans.factory.support.DisposableBean
import com.wanna.framework.context.processor.factory.BeanDefinitionRegistryPostProcessor
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

/**
 * 这是一个通用的Scope, 提供了作为一个Scope的相关的通用方法, 它是一个BeanFactoryPostProcessor, 会将自身作为Scope注册到BeanFactory当中
 */
open class GenericScope : Scope, BeanDefinitionRegistryPostProcessor, DisposableBean {

    // scopeName
    private var name = "generic"

    // 操作缓存的锁, beanName->Lock
    private val locks = ConcurrentHashMap<String, ReentrantLock>()

    // Scope Cache, 默认实现是基于ConcurrentHashMap的缓存
    private var cache = BeanLifecycleWrapperCache(StandardScopeCache())

    /**
     * 支持去进行设置设置自定义的Cache
     *
     * @param scopeCache 你想要使用的Cache
     */
    open fun setCache(scopeCache: ScopeCache) {
        this.cache = BeanLifecycleWrapperCache(scopeCache)
    }

    /**
     * 设置当前Scope的scopeName
     *
     * @param name scopeName
     */
    open fun setName(name: String) {
        this.name = name
    }

    /**
     * 获取当前scope的scopeName
     */
    open fun getName(): String {
        return this.name
    }

    /**
     * 摧毁当前Scope内的全部Bean, 并回调所有的destroy Callback
     */
    override fun destroy() {
        val beanLifecycleWrappers = this.cache.clear()
        beanLifecycleWrappers.forEach {
            val lock = locks[it.getName()]!!
            lock.lock()
            try {
                it.destroy()
            } finally {
                lock.unlock()
            }
        }
    }

    /**
     * 给定beanName, 去摧毁Scope内的一个Bean
     *
     * @param name beanName
     * @return 是否destroy成功
     */
    open fun destroy(name: String): Boolean {
        val wrapper = this.cache.remove(name)
        if (wrapper != null) {
            val lock = this.locks[wrapper.getName()]!!
            lock.lock()
            try {
                wrapper.destroy()
            } finally {
                lock.unlock()
            }
            return true
        }
        return false
    }

    override fun postProcessBeanDefinitionRegistry(registry: BeanDefinitionRegistry) {

    }

    /**
     * 在进行BeanFactory进行后置处理时, 将Scope(this)直接注册到BeanFactory当中
     */
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        beanFactory.registerScope(getName(), this)
    }

    override fun get(beanName: String, factory: ObjectFactory<*>): Any {
        // 如果必要的话, 将ObjectFactory加入到缓存当中(如果已经存在了的话, 那么不会替换之前的)
        val value = this.cache.put(beanName, BeanLifecycleWrapper(beanName, factory))
        this.locks.putIfAbsent(beanName, ReentrantLock())
        try {
            return value.getBean()
        } catch (ex: Exception) {
            throw ex
        }
    }

    /**
     * 给指定的beanName的Bean去注册Destroy的回调
     *
     * @param name beanName
     * @param callback 要设置的destroy回调
     */
    override fun registerDestructionCallback(name: String, callback: Runnable) {
        val lifecycleWrapper = this.cache.get(name) ?: return
        lifecycleWrapper.setDestroyCallback(callback)
    }

    /**
     * 移除一个当前Scope内的Bean
     *
     * @param name beanName
     * @return 移除之前存在的Bean(如果之前不存在, 有可能为null)
     */
    override fun remove(name: String): Any? {
        val lifecycleWrapper = this.cache.remove(name)
        return lifecycleWrapper?.getBean()
    }

    /**
     * 提供了ScopeCache的包装, 本来ScopeCache的是一个Object对象, 我们将它去进行扩展, 保证操作的Object对象, 都是BeanLifecycleWrapper对象; 
     *
     * @see BeanLifecycleWrapper
     * @see ScopeCache
     */
    private class BeanLifecycleWrapperCache(private val scopeCache: ScopeCache) {

        fun remove(name: String): BeanLifecycleWrapper? {
            return scopeCache.remove(name) as BeanLifecycleWrapper?
        }

        fun put(name: String, value: BeanLifecycleWrapper): BeanLifecycleWrapper {
            return this.scopeCache.put(name, value) as BeanLifecycleWrapper
        }

        fun get(name: String): BeanLifecycleWrapper? {
            return this.scopeCache.get(name) as BeanLifecycleWrapper?
        }

        fun clear(): Collection<BeanLifecycleWrapper> {
            return this.scopeCache.clear().map { it as BeanLifecycleWrapper }.toList()
        }
    }

    /**
     * 维护了一个Scope内的Bean的生命周期相关的组件, 维护了一个Bean以及它的destroy回调函数; 
     * 因为它会被加入缓存的Value当中, 应该实现自定义的equals方法, 去保证不该替换时别进行替换
     *
     * @param name beanName
     * @param objectFactory 创建Bean的factory
     */
    private class BeanLifecycleWrapper(private val name: String, private val objectFactory: ObjectFactory<*>) {
        private var callback: Runnable? = null
        private var bean: Any? = null

        fun getName(): String {
            return this.name
        }

        fun getBean(): Any {
            if (this.bean == null) {
                this.bean = objectFactory.getObject()
            }
            return this.bean!!
        }

        fun setDestroyCallback(callback: Runnable) {
            this.callback = callback
        }

        fun destroy() {
            if (this.callback != null) {
                callback!!.run()
            }
            this.callback = null
            this.bean = null
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) {
                return true
            }
            if (other == null) {
                return false
            }
            if (this.javaClass != other.javaClass) {
                return false
            }
            if (other !is BeanLifecycleWrapper) {
                return false
            }
            if (other.name != this.name) {
                return false
            }
            return true
        }

        override fun hashCode(): Int {
            return name.hashCode()
        }
    }
}