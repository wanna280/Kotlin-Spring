package com.wanna.framework.test.context.cache

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.test.context.CacheAwareContextLoaderDelegate
import com.wanna.framework.test.context.ContextLoader
import com.wanna.framework.test.context.MergedContextConfiguration
import com.wanna.framework.test.context.SmartContextLoader
import java.util.concurrent.ConcurrentHashMap

/**
 * 对于[CacheAwareContextLoaderDelegate]的默认实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 */
open class DefaultCacheAwareContextLoaderDelegate : CacheAwareContextLoaderDelegate {

    private var contextCache = ConcurrentHashMap<MergedContextConfiguration, ApplicationContext>()

    /**
     * 利用[MergedContextConfiguration]当中的[ContextLoader]进行真正的[ApplicationContext]的加载
     *
     * @param mergedContextConfiguration Merged ContextConfiguration配置信息
     * @return 根据[ContextLoader]去进行加载得到的[ApplicationContext]
     */
    protected open fun loadContextInternal(mergedContextConfiguration: MergedContextConfiguration): ApplicationContext {
        // 从MergedContextConfiguration当中获取到ContextLoader
        val contextLoader = mergedContextConfiguration.getContextLoader()
        val applicationContext: ApplicationContext

        // 如果是SmartContextLoader, 走注解的load
        // 如果不是SmartContextLoader, 走XML的load
        if (contextLoader is SmartContextLoader) {
            applicationContext = contextLoader.loadContext(mergedContextConfiguration)
        } else {
            val locations = mergedContextConfiguration.getLocations()
            applicationContext = contextLoader.loadContext(*locations)
        }
        return applicationContext
    }


    /**
     * 根据[MergedContextConfiguration]配置信息从缓存当中去获得到[ApplicationContext], 
     * 如果缓存当中还不存在对应的[ApplicationContext]的话, 那么先构建出来一个[ApplicationContext]去放入到Cache当中.
     *
     * @param mergedContextConfiguration Merged ContextConfiguration配置信息
     * @return 根据[ContextLoader]去进行加载得到的[ApplicationContext]
     */
    override fun loadApplicationContext(mergedContextConfiguration: MergedContextConfiguration): ApplicationContext {
        synchronized(contextCache) {
            var context = contextCache.get(mergedContextConfiguration)
            if (context == null) {

                // 如果缓存当中没有的话, 那么需要去进行真正的ApplicationContext的加载
                context = loadContextInternal(mergedContextConfiguration)
                contextCache.put(mergedContextConfiguration, context)
            }
            return context
        }
    }

    /**
     * 关闭Context, 从缓存当中移除对应的[ApplicationContext]
     *
     * @param mergedContextConfiguration Merged ContextConfiguration
     */
    override fun closeContext(mergedContextConfiguration: MergedContextConfiguration) {
        synchronized(this.contextCache) {
            this.contextCache.remove(mergedContextConfiguration)
        }
    }
}