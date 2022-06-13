package com.wanna.framework.transaction.support

import com.wanna.framework.core.NamedThreadLocal
import org.slf4j.LoggerFactory

/**
 * 事务同步管理器，维护了事务相关的一系列的ThreadLocal
 */
object TransactionSynchronizationManager {

    // Logger
    private val logger = LoggerFactory.getLogger(TransactionSynchronizationManager::class.java)

    // 维护事务的资源信息，比如Jdbc的事务连接对象，需要通过一个Key(比如DataSource)来去进行获取
    private val resources: ThreadLocal<MutableMap<Any, Any>> = NamedThreadLocal("Transactional resources")

    /**
     * 从ThreadLocal当中获取事务的资源(比如ConnectionHolder)
     *
     * @param key 资源的key
     * @return 获取到的资源；如果没有，return null
     */
    @JvmStatic
    fun getResource(key: Any): Any? {
        return doGetResource(key)
    }

    /**
     * 绑定一个资源给当前线程，把值设置到资源的ThreadLocal当中
     *
     * @param key key
     * @param value value
     */
    @JvmStatic
    fun bindResource(key: Any, value: Any) {
        var map = resources.get()
        if (map == null) {
            map = HashMap()
            resources.set(map)
        }
        var oldValue = map.put(key, value)
        if (oldValue is ResourceHolder && oldValue.isVoid()) {
            oldValue = null
        }
        if (oldValue != null) {
            throw IllegalStateException("在TransactionSynchronizationManager针对于当前线程[${Thread.currentThread().name}]的Key[$key]当中已经有了资源[$oldValue]去进行绑定了，不能二次绑定")
        }
        if (logger.isTraceEnabled) {
            logger.trace("将Key[$key]对应的资源[$value]绑定给了当前线程[${Thread.currentThread().name}]")
        }
    }

    /**
     * 从当前线程去取消绑定一个资源
     *
     * @param key key
     * @return 之前的资源(比如Connection)
     */
    @JvmStatic
    fun unbindResource(key: Any): Any {
        return doUnbindResource(key) ?: throw IllegalStateException("之前没有资源，无法去进行unbind")
    }

    private fun doUnbindResource(key: Any): Any? {
        val map = resources.get() ?: return null
        var value = map.remove(key)
        if (map.isEmpty()) {
            resources.remove()
        }
        if (value is ResourceHolder && value.isVoid()) {
            value = null
        }
        return value
    }

    private fun doGetResource(actualKey: Any): Any? {
        val map = resources.get() ?: return null
        var value = map[actualKey]

        // 如果是ResourceHolder，并且已经没有资源了，那么需要把资源去进行释放
        if (value is ResourceHolder && value.isVoid()) {
            map.remove(actualKey)
            if (map.isEmpty()) {
                resources.remove()
            }
            value = null
        }
        return value
    }
}