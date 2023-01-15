package com.wanna.cloud.nacos.config

import java.util.concurrent.ConcurrentHashMap

/**
 * 这是一个Nacos的PropertySource的仓库, 它是一个单例对象, 提供全局的访问
 *
 * @see NacosPropertySource
 * @see NacosPropertySourceLocator.loadIfAbsent
 */
object NacosPropertySourceRepository {

    /**
     * dataId和group的分隔符
     */
    private const val SEPARATOR = "."

    /**
     * 维护全局的NacosPropertySource, Key是dataId&group, Value是该Key对应的[NacosPropertySource]
     *
     * @see NacosPropertySource
     */
    @JvmStatic
    private val repository = ConcurrentHashMap<String, NacosPropertySource>()

    /**
     * 获取仓库当中已经保存下来的的所有的[NacosPropertySource]的列表
     *
     * @return 已经保存到仓库当中的全部[NacosPropertySource]的列表
     */
    @JvmStatic
    fun getAll(): Collection<NacosPropertySource> {
        return ArrayList(repository.values)
    }

    /**
     * 根据dataId和group去获取到已经注册的NacosPropertySource
     *
     * @param dataId dataId
     * @param group group
     * @return NacosPropertySource(如果不存在该group&dataId的NacosPropertySource, return null)
     */
    @JvmStatic
    fun getNacosPropertySource(dataId: String, group: String): NacosPropertySource? {
        return repository[getMapKey(dataId, group)]
    }

    /**
     * 往仓库当中添加一个[NacosPropertySource]
     *
     * @param propertySource 要去进行添加到仓库当中的[NacosPropertySource]
     */
    @JvmStatic
    fun registerNacosPropertySource(propertySource: NacosPropertySource) {
        repository[getMapKey(propertySource.dataId, propertySource.group)] = propertySource
    }

    /**
     * 根据dataId和group去生成Key, 去作为仓库缓存的Key
     *
     * @param dataId dataId
     * @param group group
     * @return 根据dataId和group去生成的Key
     */
    @JvmStatic
    private fun getMapKey(dataId: String, group: String): String {
        return "$group$SEPARATOR$dataId"
    }
}