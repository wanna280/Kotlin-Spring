package com.wanna.cloud.nacos.config

import java.util.concurrent.ConcurrentHashMap

/**
 * 这是一个Nacos的PropertySource的仓库，它是一个单例对象，提供全局的访问
 *
 * @see NacosPropertySource
 */
object NacosPropertySourceRepository {

    private const val SEPARATOR = "."

    // 维护全局的NacosPropertySource
    private val registry = ConcurrentHashMap<String, NacosPropertySource>()

    /**
     * 获取Nacos的PropertySource当中的全部NacosPropertySource列表
     *
     * @return 已经注册的全部NacosPropertySource
     */
    @JvmStatic
    fun getAll(): Collection<NacosPropertySource> {
        return registry.values
    }

    /**
     * 根据dataId和group去获取到已经注册的NacosPropertySource
     *
     * @param dataId dataId
     * @param group group
     * @return NacosPropertySource(如果不存在该group&dataId的NacosPropertySource，return null)
     */
    @JvmStatic
    fun getNacosPropertySource(dataId: String, group: String): NacosPropertySource? {
        return registry[getMapKey(dataId, group)]
    }

    /**
     * 往注册中心当中注册一个Nacos的PropertySource
     *
     * @param propertySource 要去进行注册的NacosPropertySource
     */
    @JvmStatic
    fun registerNacosPropertySource(propertySource: NacosPropertySource) {
        registry[getMapKey(propertySource.dataId, propertySource.group)] = propertySource
    }

    @JvmStatic
    private fun getMapKey(dataId: String, group: String): String {
        return "$group$SEPARATOR$dataId"
    }
}