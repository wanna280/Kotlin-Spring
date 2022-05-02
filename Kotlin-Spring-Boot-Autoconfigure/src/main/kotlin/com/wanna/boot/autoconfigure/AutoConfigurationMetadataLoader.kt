package com.wanna.boot.autoconfigure

import com.wanna.framework.core.io.support.PropertiesLoaderUtils
import com.wanna.framework.core.util.StringUtils
import java.util.Properties

/**
 * 这是一个AutoConfiguration的Metadata加载器，完成"META-INF/spring-autoconfigure-metadata.properties"的加载
 *
 * @see AutoConfigurationMetadata
 * @see PropertiesLoaderUtils
 */
object AutoConfigurationMetadataLoader {
    // 自动配置的元信息的path
    private const val AUTOCONFIGURATION_METADATA_PATH: String = "META-INF/spring-autoconfigure-metadata.properties"

    /**
     * 使用SPI机制从配置文件当中加载自动配置的元信息，并包装成为一个AutoConfigurationMetadata去进行返回
     */
    @JvmStatic
    fun loadMetadata(classLoader: ClassLoader): AutoConfigurationMetadata {
        val properties = PropertiesLoaderUtils.loadProperties(AUTOCONFIGURATION_METADATA_PATH, classLoader)
        return PropertiesAutoConfigurationMetadata(properties)
    }

    /**
     * 这是一个基于Properties的AutoConfigurationMetadata，获取元信息的途径是从Properties当中去进行获取
     *
     * @see AUTOCONFIGURATION_METADATA_PATH
     * @see AutoConfigurationMetadataLoader
     */
    private class PropertiesAutoConfigurationMetadata(private val properties: Properties) : AutoConfigurationMetadata {
        override fun getSet(className: String, key: String, defaultValue: Set<String>?): Set<String>? {
            val metaStr = get(className, key) ?: return defaultValue
            return LinkedHashSet(StringUtils.commaDelimitedListToStringArray(metaStr).toList())
        }

        override fun getSet(className: String, key: String): Set<String>? {
            val metaStr = get(className, key) ?: return null
            return LinkedHashSet(StringUtils.commaDelimitedListToStringArray(metaStr).toList())
        }

        override fun get(className: String, key: String, defaultValue: String?): String? {
            return get(className, key) ?: defaultValue
        }

        override fun get(className: String, key: String): String? {
            return properties["${className}.${key}"]?.toString()
        }
    }
}