package com.wanna.cloud.nacos.config

import java.util.concurrent.ConcurrentHashMap

/**
 * 这是一个Nacos的PropertySourceRegistry，它是一个单例对象，提供全局的访问
 */
object NacosPropertySourceRepository {

    private const val SEPARATOR = "."

    private val registry = ConcurrentHashMap<String, NacosPropertySource>()

    fun getAll(): Collection<NacosPropertySource> {
        return registry.values
    }

    fun getNacosPropertySource(dataId: String, group: String): NacosPropertySource? {
        return registry[getMapKey(dataId, group)]
    }

    fun registerNacosPropertySource(propertySource: NacosPropertySource) {
        registry[getMapKey(propertySource.dataId, propertySource.group)] = propertySource
    }

    private fun getMapKey(dataId: String, group: String): String {
        return "$group$SEPARATOR$dataId"
    }
}