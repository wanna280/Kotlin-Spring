package com.wanna.boot.env

import com.wanna.framework.core.environment.PropertySource
import com.wanna.framework.core.io.Resource
import com.wanna.framework.core.io.support.PropertiesLoaderUtils
import java.util.Collections

/**
 * 这是一个Properties的PropertySourceLoader, 主要负责去使用Jdk提供的Properties去加载".properties"配置文件信息
 *
 * @see PropertySourceLoader
 * @see PropertiesLoaderUtils
 * @see OriginTrackedMapPropertySource
 */
open class PropertiesPropertySourceLoader : PropertySourceLoader {

    /**
     * 支持去处理properties和xml的文件扩展名
     *
     * @return "xml" and "properties"
     */
    override fun getFileExtensions() = arrayOf("properties", "xml")

    @Suppress("UNCHECKED_CAST")
    override fun load(name: String, resource: Resource): List<PropertySource<*>> {
        val properties = loadProperties(resource)
        if (properties.isEmpty()) {
            return Collections.emptyList()
        }
        val propertySources = ArrayList<PropertySource<*>>()
        for (index in properties.indices) {
            val documentNumber = if (properties.size == 1) "" else "(document #$index)"
            propertySources += OriginTrackedMapPropertySource(name + documentNumber, properties[index])
        }
        return propertySources
    }

    /**
     * 根据给定的Resource, 去加载到属性配置列表
     *
     * @param resource Resource
     * @return 加载到的属性列表
     */
    @Suppress("UNCHECKED_CAST")
    private fun loadProperties(resource: Resource): List<Map<String, Any>> {
        val result = ArrayList<Map<String, Any>>()
        val properties = PropertiesLoaderUtils.loadProperties(resource)
        result.add(properties as Map<String, Any>)
        return result
    }
}