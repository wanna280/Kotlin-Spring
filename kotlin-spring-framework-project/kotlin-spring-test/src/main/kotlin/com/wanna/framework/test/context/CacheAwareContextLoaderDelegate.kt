package com.wanna.framework.test.context

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.test.context.cache.DefaultCacheAwareContextLoaderDelegate

/**
 * 使用[ContextLoader]去进行委托, 完成[ApplicationContext]加载;
 * 对于[ApplicationContext]提供基于[MergedContextConfiguration]去进行缓存的功能,
 * 根据[MergedContextConfiguration]即可获取到缓存当中已经存放起来的[ApplicationContext,
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/5
 * @see DefaultCacheAwareContextLoaderDelegate
 * @see ContextLoader
 */
interface CacheAwareContextLoaderDelegate {

    /**
     * 根据[MergedContextConfiguration]配置信息去加载得到[ApplicationContext];
     * 最终会使用[MergedContextConfiguration]当中的[ContextLoader], 借助[MergedContextConfiguration]当中的配置信息,
     * 从而完成[ApplicationContext]的构建和加载, 最终返回一个已经完成初始化和刷新的[ApplicationContext]
     *
     * @param mergedContextConfiguration Merged ContextConfiguration
     * @return 根据[MergedContextConfiguration]去加载得到的[ApplicationContext]
     */
    fun loadApplicationContext(mergedContextConfiguration: MergedContextConfiguration): ApplicationContext

    /**
     * 关闭Context, 根据[MergedContextConfiguration]从Cache当中去移除掉对应的[ApplicationContext]
     *
     * @param mergedContextConfiguration Merged ContextConfiguration
     */
    fun closeContext(mergedContextConfiguration: MergedContextConfiguration)
}