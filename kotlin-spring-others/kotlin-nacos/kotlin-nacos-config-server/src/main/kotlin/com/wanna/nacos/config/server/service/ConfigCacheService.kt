package com.wanna.nacos.config.server.service

import com.wanna.nacos.api.common.Constants
import com.wanna.nacos.api.notify.NotifyCenter
import com.wanna.nacos.api.utils.Md5Utils
import com.wanna.nacos.config.server.model.CacheItem
import com.wanna.nacos.config.server.model.event.LocalDataChangeEvent
import com.wanna.nacos.config.server.utils.GroupKey2
import java.util.concurrent.ConcurrentHashMap

/**
 * ConfigCacheService, 维护ConfigServer当中的配置文件的状态信息;
 *
 * 当ConfigServer当中的配置文件发生变更时, 就需要通过发布[LocalDataChangeEvent]去通知所有的监听器去处理这个事件
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/13
 *
 * @see LocalDataChangeEvent
 */
object ConfigCacheService {

    /**
     * CacheItem缓存, 用于维护配置文件的MD5值的缓存
     */
    @JvmStatic
    private val CACHE = ConcurrentHashMap<String, CacheItem>()

    /**
     * 确保给定的groupKey的CacheItem一定已经存在
     *
     * @param groupKey groupKey
     * @return 缓存当中已经存在的CacheItem(如果之前已经有, 那么返回之前的; 如果之前没有, 返回一个新的CacheItem)
     */
    @JvmStatic
    private fun makeSure(groupKey: String): CacheItem {
        var cacheItem = CACHE[groupKey]
        if (cacheItem != null) {
            return cacheItem
        }
        val item = CacheItem(groupKey)

        // 这里需要使用putIfAbsent, 因为有可能出现别的线程已经放入一个CacheItem进去了...
        // 但是我们这里的item是新创建的, 因此如果我们放入成功了, 那么使用我们自己的CacheItem;
        // 如果放入失败了...那么使用已经存在的CacheItem
        cacheItem = CACHE.putIfAbsent(groupKey, item)
        return cacheItem ?: item
    }

    /**
     * 转存变更情况
     *
     * @param dataId dataId
     * @param group group
     * @param tenant tenant(namespace)
     * @param lastModifiedTs 上次修改的时间
     */
    @JvmStatic
    fun dumpChange(dataId: String, group: String, tenant: String, content: String, lastModifiedTs: Long): Boolean {
        val groupKey = GroupKey2.getKeyTenant(dataId, group, tenant)
        // 确保groupKey对应的CacheItem已经存在...
        makeSure(groupKey)
        // TODO 这里应该加锁...
        try {
            val md5 = Md5Utils.md5Hex(content, Constants.ENCODE)
            updateMd5(groupKey, md5, lastModifiedTs)
        } catch (ex: Exception) {
            return false
        }
        return true
    }

    /**
     * 获取给定的groupKey的CacheItem的MD5值
     *
     * @param groupKey groupKey
     * @return CacheItem的MD5值(不存在的话, 返回""空字符串)
     */
    @JvmStatic
    fun getContentMd5(groupKey: String): String {
        return CACHE[groupKey]?.md5 ?: ""
    }

    /**
     * 根据groupKey去更新缓存当中的CacheItem的MD5
     *
     * @param groupKey groupKey
     * @param md5 需要去更新成为的MD5
     * @param lastModifiedTs 上次修改时间
     */
    @JvmStatic
    fun updateMd5(groupKey: String, md5: String, lastModifiedTs: Long) {
        val cacheItem = makeSure(groupKey)
        if (cacheItem.md5 != md5) {
            cacheItem.md5 = md5
            cacheItem.lastModifiedTs = lastModifiedTs

            // 发布配置文件已经发生变更的事件, 需要通知所有的客户端...
            NotifyCenter.publishEvent(LocalDataChangeEvent(groupKey))
        }
    }

    /**
     * 移除一个CacheItem
     *
     * @param dataId dataId
     * @param group group
     * @param tenant tenant
     * @return 是否删除成功?
     */
    @JvmStatic
    fun remove(dataId: String, group: String, tenant: String): Boolean {
        // TODO 这里似乎得加上锁的逻辑?
        val groupKey = GroupKey2.getKeyTenant(dataId, group, tenant)
        CACHE.remove(groupKey)

        // 当配置文件被移除时, 我们也得通知所有的客户端...
        NotifyCenter.publishEvent(LocalDataChangeEvent(groupKey))
        return true
    }

    /**
     * 检查给定groupKey对应的文件的Md5是否已经是最新的了?
     *
     * @param groupKey groupKey(dataId&group/tenant)
     * @param clientMd5 当前客户端的MD5值
     * @param ip 客户端IP
     * @param tag tag
     * @return 如果你给定的MD5和Cache当中的MD5完全一致的话, return true; 否则return false
     */
    @JvmStatic
    fun isUptodate(groupKey: String, clientMd5: String, ip: String, tag: String): Boolean {
        return (getContentCache(groupKey)?.md5 ?: "") == clientMd5
    }

    /**
     * 检查给定groupKey对应的文件的Md5是否已经是最新的了?
     *
     * @param groupKey groupKey(dataId&group/tenant)
     * @param clientMd5 当前客户端的MD5值
     * @return 如果你给定的MD5和Cache当中的MD5完全一致的话, return true; 否则return false
     */
    @JvmStatic
    fun isUptodate(groupKey: String, clientMd5: String): Boolean {
        return (getContentCache(groupKey)?.md5 ?: "") == clientMd5
    }

    /**
     * 获取ConfigFile内容的缓存
     *
     * @param groupKey groupKey(dataId&group&tenant)
     */
    @JvmStatic
    fun getContentCache(groupKey: String): CacheItem? = CACHE[groupKey]

}