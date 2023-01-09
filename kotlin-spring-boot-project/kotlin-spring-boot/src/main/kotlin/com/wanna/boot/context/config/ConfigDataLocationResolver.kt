package com.wanna.boot.context.config

import com.wanna.framework.lang.Nullable
import java.util.Collections

/**
 * ConfigDataLocation的Resolver, 负责将一个ConfigDataLocation, 去解析成为对应的ConfigDataResource
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 *
 * @param T 支持去处理的ConfigDataResource的类型
 */
interface ConfigDataLocationResolver<R : ConfigDataResource> {

    /**
     * 检查给定的ConfigDataLocation, 当前Resolver能否支持去进行处理?
     *
     * @param context context
     * @param location location
     * @return 如果支持去进行解析, return true; 否则return false
     */
    fun isResolvable(@Nullable context: ConfigDataLocationResolverContext?, location: ConfigDataLocation): Boolean

    /**
     * 执行真正的解析, 将ConfigDataLocation解析成为ConfigDataResource
     *
     * @param context context
     * @param location location
     * @return 执行解析的结果, 得到的ConfigDataResource列表
     */
    fun resolve(@Nullable context: ConfigDataLocationResolverContext?, location: ConfigDataLocation): List<R>

    /**
     * 解析给定的Profiles的ConfigDataLocation
     *
     * @param context context
     * @param location location
     * @param profiles Profiles
     * @return 执行解析的结果, 得到的ConfigDataResource
     */
    fun resolveProfileSpecific(
        @Nullable context: ConfigDataLocationResolverContext?,
        location: ConfigDataLocation,
        profiles: Profiles
    ): List<R> {
        return Collections.emptyList()
    }
}