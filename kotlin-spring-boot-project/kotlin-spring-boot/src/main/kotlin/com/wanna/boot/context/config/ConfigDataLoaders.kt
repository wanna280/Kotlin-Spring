package com.wanna.boot.context.config

import com.wanna.boot.ConfigurableBootstrapContext
import com.wanna.framework.core.ResolvableType
import com.wanna.framework.core.io.support.SpringFactoriesLoader
import org.slf4j.LoggerFactory
import java.util.*

/**
 * 组合了多个ConfigDataLoader, 去提供ConfigData的加载
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/8
 *
 * @see ConfigDataLoader
 */
class ConfigDataLoaders(private val bootstrapContext: ConfigurableBootstrapContext) {

    /**
     * Logger
     */
    private val logger = LoggerFactory.getLogger(ConfigDataLoader::class.java)

    /**
     * ConfigDataLoaders
     */
    private val loaders: List<ConfigDataLoader<*>>

    /**
     * 统计出来所有的这些ConfigDataLoader, 一共支持去进行加载的资源类型列表
     */
    private val resourceTypes: List<Class<*>>

    init {
        // 使用SpringFactoriesLoader去加载ConfigDataLoader
        @Suppress("UNCHECKED_CAST")
        this.loaders = SpringFactoriesLoader.loadFactories(ConfigDataLoader::class.java)

        // 统计出来这些ConfigDataLoader所能支持去进行的资源类型
        this.resourceTypes = getResourceTypes(this.loaders)
    }

    /**
     * 根据给定的ConfigDataLoader, 去解析出来所有的资源类型
     *
     * @param loaders loaders
     * @return 支持去进行处理的资源类型列表
     */
    private fun getResourceTypes(loaders: Collection<ConfigDataLoader<*>>): List<Class<*>> {
        val resourceTypes = ArrayList<Class<*>>()
        for (loader in loaders) {
            resourceTypes.add(getResourceType(loader))
        }
        return Collections.unmodifiableList(resourceTypes)
    }

    /**
     * 解析给定的ConfigDataLoader支持去进行加载的资源类型(通过解析ConfigDataLoader的泛型的方式来实现)
     *
     * @param loader ConfigDataLoader
     * @return 支持去进行加载的资源类型
     */
    private fun getResourceType(loader: ConfigDataLoader<*>): Class<*> {
        val resolvableType = ResolvableType.forClass(loader.javaClass).`as`(ConfigDataLoader::class.java)
        return resolvableType.getGenerics()[0].resolve()!!
    }

    /**
     * 对于给定的Resource去提供加载, 并得到的ConfigData
     *
     * @param context context
     * @param resource 需要去进行加载的资源
     * @return 加载得到的ConfigData
     */
    fun <R : ConfigDataResource> load(context: ConfigDataLoaderContext, resource: R): ConfigData {
        val loader = getLoader(context, resource)
        if (logger.isTraceEnabled) {
            logger.trace("Loading $resource using loader ${loader.javaClass.name}")
        }
        return loader.load(context, resource)
    }

    /**
     * 为给定的ConfigDataResource, 去找到合适的ConfigDataLoader去执行加载
     *
     * @param context context
     * @param resource resource
     * @return 去提供该资源的加载的ConfigDataLoader
     */
    @Suppress("UNCHECKED_CAST")
    private fun <R : ConfigDataResource> getLoader(
        context: ConfigDataLoaderContext,
        resource: R
    ): ConfigDataLoader<R> {
        var result: ConfigDataLoader<R>? = null

        // 根据所有的资源类型, 去进行匹配...如果有支持去处理该类型的Resource的话, 选用它去进行资源的加载
        for (index in this.resourceTypes.indices) {
            val loader = loaders[index]
            if (this.resourceTypes[index].isInstance(resource)) {
                if ((loader as ConfigDataLoader<R>).isLoadable(context, resource)) {
                    // 如果找到了多个Loader都支持去处理该类型资源的加载, 那么丢出异常
                    if (result != null) {
                        throw IllegalStateException(
                            ("Multiple loaders found for resource '" + resource + "' [" + loader.javaClass.name) + "," + result.javaClass.name + "]"
                        )
                    }
                    result = loader
                }

            }
        }
        return result ?: throw IllegalStateException("No loader found for resource '$resource'")
    }

}