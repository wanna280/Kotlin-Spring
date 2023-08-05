package com.wanna.boot.context.config

/**
 * ConfigData的Loader, 将给定的ConfigDataResource, 去加载成为一个ConfigData
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 */
interface ConfigDataLoader<R : ConfigDataResource> {

    /**
     * 是否可以可以加载给定的Resource?
     *
     * @param context context
     * @param resource resource
     * @return 如果支持去加载该Resource, 那么return true; 否则return false
     */
    fun isLoadable(context: ConfigDataLoaderContext, resource: R): Boolean = true

    /**
     * 自行真正的Resource的加载
     *
     * @param context context
     * @param resource 要去进行加载的Resource
     * @return 加载得到的ConfigData
     */
    fun load(context: ConfigDataLoaderContext, resource: R): ConfigData
}