package com.wanna.boot.env

import com.wanna.framework.core.environment.PropertySource
import com.wanna.framework.core.io.Resource
import com.wanna.framework.core.io.support.PropertiesLoaderUtils

/**
 * 这是一个Properties的PropertySourceLoader，负责去使用Jdk提供的Properties去加载Properties配置文件信息
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
    override fun load(name: String, resource: String): List<PropertySource<*>> {
        val propertySources = ArrayList<PropertySource<*>>()
        val properties = PropertiesLoaderUtils.loadProperties(resource)
        propertySources += OriginTrackedMapPropertySource(name, properties as Map<String, Any>)
        return propertySources
    }

    @Suppress("UNCHECKED_CAST")
    override fun load(name: String, resource: Resource): List<PropertySource<*>> {
        val propertySources = ArrayList<PropertySource<*>>()
        val properties = PropertiesLoaderUtils.loadProperties(resource)
        propertySources += OriginTrackedMapPropertySource(name, properties as Map<String, Any>)
        return propertySources
    }
}