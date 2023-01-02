package com.wanna.framework.core.type.classreading

import com.wanna.framework.core.io.DefaultResourceLoader
import com.wanna.framework.core.io.Resource
import com.wanna.framework.core.io.ResourceLoader
import java.util.concurrent.ConcurrentMap

/**
 * 带缓存的SimpleMetadataReaderFactory实现, 为MetadataReader的读取提供缓存
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/1
 *
 * @param resourceLoader ResourceLoader
 */
open class CachingMetadataReaderFactory(resourceLoader: ResourceLoader) : SimpleMetadataReaderFactory(resourceLoader) {
    companion object {
        /**
         * 默认的缓存的最大数量限制
         */
        private const val DEFAULT_CACHE_LIMIT = 256
    }

    constructor() : this(DefaultResourceLoader())

    /**
     * MetadataReader Cache, 为已经读取过的类的MetadataReader提供一层缓存, 避免重复读取
     */
    private var metadataReaderCache: MutableMap<Resource, MetadataReader>? = null

    init {
        this.setCacheLimit(DEFAULT_CACHE_LIMIT)
    }

    /**
     * 设置缓存的最大数量限制, 默认值为256
     *
     * @param cacheLimit cacheLimit
     */
    open fun setCacheLimit(cacheLimit: Int) {
        if (cacheLimit <= 0) {
            metadataReaderCache = null
        } else {
            this.metadataReaderCache = LocalResourceCache(cacheLimit)
        }
    }

    /**
     * 当通过Resource去获取该类的MetadataReader的话, 先尝试从Cache当中去进行获取
     *
     * @param resource Resource
     * @return MetadataReader
     */
    override fun getMetadataReader(resource: Resource): MetadataReader {
        if (metadataReaderCache is ConcurrentMap<*, *>) {
            return getCacheOrCreate(resource)
        } else if (metadataReaderCache is LocalResourceCache) {
            synchronized(this.metadataReaderCache!!) {
                return getCacheOrCreate(resource)
            }
        } else {
            return super.getMetadataReader(resource)
        }
    }

    /**
     * 从缓存当中去获取到MetadataReader
     *
     * @param resource Resource
     * @return MetadataReader
     */
    private fun getCacheOrCreate(resource: Resource): MetadataReader {
        val metadataReaderCache = this.metadataReaderCache ?: throw IllegalStateException("No MetadataReader Cache!!!")
        var metadataReader = metadataReaderCache[resource]
        if (metadataReader == null) {
            metadataReader = super.getMetadataReader(resource)
            metadataReaderCache[resource] = metadataReader
        }
        return metadataReader
    }

    /**
     * LocalResource的缓存, 采用LRU的方式去实现
     *
     * @param cacheLimit 缓存的大小限制
     */
    private class LocalResourceCache(@Volatile var cacheLimit: Int) :
        LinkedHashMap<Resource, MetadataReader>(cacheLimit, 0.75f, true) {

        /**
         * 当size>cacheLimit时, 需要移除掉旧的元素
         *
         * @param eldest 旧的元素
         * @return size>cacheLimit?
         */
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Resource, MetadataReader>) = size > cacheLimit
    }
}