package com.wanna.boot.context.config

import com.wanna.framework.lang.Nullable

/**
 * ConfigData的Importer, 负责去进行真正的配置文件的加载
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 *
 * @param resolvers 提供ConfigDataLocation解析成为资源的Resolvers
 * @param loaders 提供将资源去加载成为对应的ConfigData的Loaders
 */
class ConfigDataImporter(
    private val loaders: ConfigDataLoaders,
    private val resolvers: ConfigDataLocationResolvers
) {

    /**
     * 已经加载的ConfigDataLocations
     */
    val loadedLocations = LinkedHashSet<ConfigDataLocation>()

    /**
     * 可选的ConfigDataLocations
     */
    val optionalLocations = LinkedHashSet<ConfigDataLocation>()

    /**
     * 已经加载过的ConfigDataResource
     */
    private val loaded = LinkedHashSet<ConfigDataResource>()

    /**
     * 对于给定的这些ConfigDataLocation去执行解析和加载
     *
     * @param activationContext ActivationContext
     * @param resolverContext ConfigDataLocationResolver解析配置文件需要用到的上下文信息
     * @param loaderContext ConfigDataLoader去加载配置文件需要用到的上下文信息
     * @param imports 待进行解析和加载的ConfigDataLocations
     * @return 解析和加载完成得到的结果(Key-Resolver去解析ConfigDataLocation的结果, Value-Loader加载配置文件的ConfigData结果)
     */
    fun resolveAndLoad(
        @Nullable activationContext: ConfigDataActivationContext?,
        resolverContext: ConfigDataLocationResolverContext,
        loaderContext: ConfigDataLoaderContext,
        imports: List<ConfigDataLocation>
    ): Map<ConfigDataResolutionResult, ConfigData> {
        val profiles = activationContext?.profiles

        // 利用ConfigDataLocationResolvers去解析给定的这些位置的配置文件...
        // 这里会分别去尝试, 在有Profiles/没有Profiles这两种情况下去进行加载
        val resolved = resolve(resolverContext, profiles, imports)

        // 利用ConfigDataLoaders对给定的这些ConfigDataResolutionResult去进行加载
        // 对于同一个文件有可能会加载多次(多个阶段都加载了无Profiles的配置文件, 这里会基于hashCode&equals去实现去重...)
        return load(loaderContext, resolved)
    }

    /**
     * 利用ConfigDataLocationResolvers对给定的这些ConfigDataLocations去进行解析
     *
     * @param locationResolverContext LocationResolverContext
     * @param profiles Profiles
     * @param locations 待进行解析的locations
     */
    private fun resolve(
        locationResolverContext: ConfigDataLocationResolverContext,
        @Nullable profiles: Profiles?,
        locations: List<ConfigDataLocation>
    ): List<ConfigDataResolutionResult> {
        val result = ArrayList<ConfigDataResolutionResult>()

        for (location in locations) {
            result += resolve(locationResolverContext, profiles, location)
        }

        return result
    }

    /**
     * 对单个ConfigDataLocation去进行解析, 解析成为合适的Resource
     *
     * @param profiles Profiles(为null代表不带Profiles的解析)
     * @param location 待进行解析的配置文件的位置
     * @return 从Location位置去找到的合适的资源列表
     */
    private fun resolve(
        locationResolverContext: ConfigDataLocationResolverContext,
        @Nullable profiles: Profiles?,
        location: ConfigDataLocation
    ): List<ConfigDataResolutionResult> {
        return resolvers.resolve(locationResolverContext, profiles, location)
    }

    /**
     * 利用ConfigDataLoaders对给定的这些ConfigDataResolutionResult去进行加载
     *
     * @param loaderContext LoaderContext
     * @param candidates 候选的要去进行加载的资源信息
     * @return 加载得到[ConfigData]的结果
     */
    private fun load(
        loaderContext: ConfigDataLoaderContext,
        candidates: List<ConfigDataResolutionResult>
    ): Map<ConfigDataResolutionResult, ConfigData> {
        val result = LinkedHashMap<ConfigDataResolutionResult, ConfigData>()
        for (index in candidates.indices.reversed()) {
            val candidate = candidates[index]
            val location = candidate.location
            val resource = candidate.resource
            if (resource.optional) {
                this.optionalLocations.add(location)
            }

            // 如果之前已经收集过这个Resource, 那么无需去进行重复的收集...
            // 基于hashCode&equals方法去实现去重...
            if (loaded.contains(resource)) {
                this.loadedLocations.add(location)


                // 如果之前还没收集过这个Resource的话, 那么我们才需要去进行收集起来...
            } else {
                val configData = this.loaders.load(loaderContext, resource)
                this.loaded += resource
                this.loadedLocations.add(location)
                result[candidate] = configData
            }
        }
        return result
    }

}