package com.wanna.boot.context.config

import com.wanna.framework.core.io.support.SpringFactoriesLoader
import com.wanna.framework.lang.Nullable
import java.util.function.Supplier

/**
 * 组合了多个ConfigDataLocationResolver, 提供了ConfigDataLocation的加载
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 *
 * @see ConfigDataLocationResolver
 */
class ConfigDataLocationResolvers {

    /**
     * ConfigDataLocation Resolvers
     */
    private val resolvers = SpringFactoriesLoader.loadFactories(ConfigDataLocationResolver::class.java)

    /**
     * 利用ConfigDataLocationResolver去进行解析给定的ConfigDataLocation
     *
     * @param context context
     * @param profiles profiles
     * @param location location
     * @return 针对该Location去解析到的资源列表
     */
    fun resolve(
        context: ConfigDataLocationResolverContext,
        @Nullable profiles: Profiles?,
        @Nullable location: ConfigDataLocation?
    ): List<ConfigDataResolutionResult> {
        location ?: return emptyList()

        for (resolver in this.resolvers) {
            if (resolver.isResolvable(context, location)) {
                return resolve(resolver, context, profiles, location)
            }
        }
        throw UnsupportedConfigDataLocationException(location)
    }

    private fun resolve(
        resolver: ConfigDataLocationResolver<*>,
        context: ConfigDataLocationResolverContext,
        @Nullable profiles: Profiles?,
        location: ConfigDataLocation
    ): List<ConfigDataResolutionResult> {
        // 先进行没有profiles的解析
        val resolved = resolve(location, false) { resolver.resolve(context, location) }
        if (profiles == null) {
            return resolved
        }

        // 再进行有profiles的解析
        val profileSpec = resolve(location, true) { resolver.resolveProfileSpecific(context, location, profiles) }
        return resolved + profileSpec
    }

    private fun resolve(
        location: ConfigDataLocation,
        profileSpecific: Boolean,
        resolveAction: Supplier<List<ConfigDataResource>>
    ): List<ConfigDataResolutionResult> {
        val result = ArrayList<ConfigDataResolutionResult>()
        val configDataResources = resolveAction.get()
        for (resource in configDataResources) {
            result += ConfigDataResolutionResult(resource, location, profileSpecific)
        }
        return result
    }
}