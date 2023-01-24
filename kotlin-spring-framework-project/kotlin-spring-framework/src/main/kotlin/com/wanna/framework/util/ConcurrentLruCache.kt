package com.wanna.framework.util

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.function.Function

/**
 * 支持并发访问的LRU Cache
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/24
 *
 * @param sizeLimit 缓存的最大Size限制
 * @param generator 对于给定的Key, 如何去进行Value的生成
 */
open class ConcurrentLruCache<K : Any, V : Any>(val sizeLimit: Int, private val generator: Function<K, V>) {

    /**
     * Cache
     */
    private val cache = ConcurrentHashMap<K, V>()

    /**
     * Queue
     */
    private val queue = ConcurrentLinkedDeque<K>()

    /**
     * 访问缓存用到的读写锁
     */
    private val readWriteLock = ReentrantReadWriteLock()

    /**
     * 当前缓存当中的元素的数量
     */
    @Volatile
    var size = 0
        private set

    init {
        if (sizeLimit < 0) {
            throw IllegalStateException("Cache size limit must not be negative")
        }
    }

    /**
     * 检查缓存当中是否已经包含了这样的Key的元素缓存?
     *
     * @param key key
     * @return 如果缓存当中已经包含该元素, return true; 否则return false
     */
    open fun contains(key: K): Boolean = this.cache.containsKey(key)

    /**
     * 通过Key从Cache当中去获取元素
     *
     * @param key key
     * @return Value
     */
    open fun get(key: K): V {
        // 如果sizeLimit=0, 说明不允许缓存, 那么直接使用generator去进行生成和返回
        if (sizeLimit == 0) {
            return generator.apply(key)
        }

        // 如果sizeLimit不为0, 那么可以尝试从缓存当中去获取元素
        var cached = this.cache[key]

        // 如果缓存当中已经有了该元素的话, 那么需要将该元素
        if (cached != null) {

            // 如果容量还没满的话, 那么直接返回就行...
            if (this.size < this.sizeLimit) {
                return cached
            }

            // 如果容量已经满了, 那么需要将该元素放到队尾去, 它是最新被访问的...(这里不用加写锁, 因为并未删除队列当中的元素, 也不会产生对于读的影响)
            this.readWriteLock.readLock().lock()
            try {
                // 如果队列当中存在有该元素的话, 那么把该元素放到队列尾部去, 队列尾部说明数据越信息
                if (this.queue.removeLastOccurrence(key)) {
                    this.queue.offer(key)
                }
                return cached
            } finally {
                this.readWriteLock.readLock().unlock()
            }
        }

        // 如果缓存当中还没有该元素的话, 那么需要去构建并加入到缓存当中
        this.readWriteLock.writeLock().lock()

        try {
            // 加锁之后必定需要去重新从缓存里面拿, 避免这个间隙被塞进去元素了...重新检查一波
            cached = cache[key]

            // 如果这一刻已经缓存当中已经有了的话, 那么调整队列当中的元素的顺序, 直接返回即可
            if (cached != null) {
                if (this.queue.removeLastOccurrence(key)) {
                    this.queue.offer(key)
                }
                return cached
            }

            // 如果加锁之后再从缓存去拿, 仍然还是没有的话, 那么真的需要去进行构建了...
            val value = this.generator.apply(key)

            // 如果之前的空间已经满了, 需要先清理一下缓存空间...那么将最老的元素从queue&cache当中去移除掉
            if (this.size == this.sizeLimit) {
                val lastUsed = this.queue.poll()
                if (lastUsed != null) {
                    this.cache.remove(lastUsed)
                }
            }

            // 把新构建出来的元素添加到queue&cache当中去...
            this.queue.offer(key)
            this.cache[key] = value
            this.size = this.cache.size
            return value
        } finally {
            this.readWriteLock.writeLock().unlock()
        }
    }

    /**
     * 从缓存当中移除给定Key的元素
     *
     * @param key key
     * @return 之前是否存在过这样的元素?
     */
    open fun remove(key: K): Boolean {
        this.readWriteLock.writeLock().lock()
        try {
            val wasPresent = this.cache.remove(key) != null
            this.queue.remove(key)
            this.size = this.queue.size
            return wasPresent
        } finally {
            this.readWriteLock.writeLock().unlock()
        }
    }


    /**
     * 清空当前缓存当中的所有元素
     */
    open fun clear() {
        this.readWriteLock.writeLock().lock()
        try {
            this.queue.clear()
            this.cache.clear()
            this.size = 0
        } finally {
            this.readWriteLock.writeLock().unlock()
        }
    }

}