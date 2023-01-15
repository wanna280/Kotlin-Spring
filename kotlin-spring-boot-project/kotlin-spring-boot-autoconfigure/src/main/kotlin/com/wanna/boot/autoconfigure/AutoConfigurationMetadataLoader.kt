package com.wanna.boot.autoconfigure

import com.wanna.framework.core.io.support.PropertiesLoaderUtils
import com.wanna.framework.util.StringUtils
import java.util.*

/**
 * 这是一个AutoConfiguration的Metadata加载器, 完成"META-INF/spring-autoconfigure-metadata.properties"的加载;
 * 可以最快速度地去完成自动配置类是否要导入到容器当中的匹配工作, 可以不读取字节码的方式, 就可以去排除掉大量的自动配置类, 可以使用这种方式去排除掉绝大多数的自动配置类;
 * 比如SpringBoot当中的OnBeanCondition/OnClassCondition, 就可以在这里去完成快速地对配置类去进行过滤;
 *
 * @see AutoConfigurationMetadata
 * @see PropertiesLoaderUtils
 */
object AutoConfigurationMetadataLoader {
    /**
     * 自动配置的元信息配置文件的路径(class path)
     */
    private const val AUTOCONFIGURATION_METADATA_PATH: String = "META-INF/spring-autoconfigure-metadata.properties"

    /**
     * 使用SPI机制从配置文件当中加载自动配置的元信息, 并包装成为一个AutoConfigurationMetadata去进行返回;
     * AutoConfigurationMetadata主要是提供自动配置类的信息对应Properties的快速读取
     *
     * @param classLoader 要去进行AutoConfigurationMetadata的加载的ClassLoader
     * @return 加载得到的AutoConfigurationMetadata
     */
    @JvmStatic
    fun loadMetadata(classLoader: ClassLoader): AutoConfigurationMetadata {
        // 使用PropertiesLoaderUtils去加载META-INF/spring-autoconfigure-metadata.properties
        val properties = PropertiesLoaderUtils.loadAllProperties(AUTOCONFIGURATION_METADATA_PATH, classLoader)

        // 将Properties包装成为PropertiesAutoConfigurationMetadata
        return PropertiesAutoConfigurationMetadata(properties)
    }

    /**
     * 这是一个基于Properties的AutoConfigurationMetadata, 获取元信息的途径是从给定的Properties(java.util)当中去进行获取;
     * 可以使用指定的方式去进行获取配置的内容, 一般是通过"{className}.{key}"去获取到配置的具体信息;
     * 如果必要的话, 那么需要使用",", 将配置的具体信息切割成为Set去进行返回;
     *
     * @see AUTOCONFIGURATION_METADATA_PATH
     * @see AutoConfigurationMetadataLoader
     */
    private class PropertiesAutoConfigurationMetadata(private val properties: Properties) : AutoConfigurationMetadata {

        /**
         * 只要可以加载到Properties, 并且Properties当中存在有该className作为Key的话, 那么就说明是被处理过了
         *
         * @param className className
         * @return 如果Properties当中包含该className, 说明被处理过了, return true; 否则return false
         */
        override fun wasProcessed(className: String): Boolean = properties.containsKey(className)

        override fun getSet(className: String, key: String, defaultValue: Set<String>): Set<String> {
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

        override fun getInt(className: String, key: String, defaultValue: Int): Int {
            return getInt(className, key) ?: defaultValue
        }

        override fun getInt(className: String, key: String): Int? {
            return get(className, key)?.toInt()
        }

        override fun get(className: String, key: String): String? {
            return properties["${className}.${key}"]?.toString()
        }
    }
}