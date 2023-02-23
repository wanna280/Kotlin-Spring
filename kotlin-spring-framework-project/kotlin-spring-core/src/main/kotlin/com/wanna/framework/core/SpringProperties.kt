package com.wanna.framework.core

import com.wanna.framework.lang.Nullable
import java.util.*

/**
 * 使用static的方式去维护SpringProperties的属性配置信息.
 *
 * 通过读取"spring.properties"配置文件去填充localProperties, 也允许使用
 * 手动的编程的方式去进行localProperties的填充.
 *
 * 对于getProperty去获取属性值时, 会优先从localProperties当中去进行获取,
 * 如果获取不到的话, 那么将会尝试从JVM级别的系统属性SystemProperties当中去进行读取.
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/13
 */
object SpringProperties {

    /**
     * 属性的资源位置
     */
    private const val PROPERTIES_RESOURCE_LOCATION = "spring.properties"

    /**
     * 存放加载的"spring.properties"配置文件的配置信息
     */
    @JvmStatic
    private val localProperties = Properties()

    init {
        try {
            val classLoader = SpringProperties.javaClass.classLoader
            val url =
                if (classLoader == null) ClassLoader.getSystemResource(PROPERTIES_RESOURCE_LOCATION)
                else classLoader.getResource(PROPERTIES_RESOURCE_LOCATION)

            url?.openStream()?.use(localProperties::load)
        } catch (ex: Exception) {
            // No logger available, use System.err to print
            System.err.println("Could not load 'spring.properties' file from local classpath, ex: $ex")
        }
    }


    /**
     * 使用编程式的方式去设置一个局部的属性值, 将会覆盖"spring.properties"当中的配置信息
     *
     * @param key key
     * @param value value(null代表remove, 不为null代表覆盖)
     */
    @JvmStatic
    fun setProperty(key: String, @Nullable value: String?) {
        if (value != null) {
            localProperties.setProperty(key, value)
        } else {
            localProperties.remove(key)
        }
    }

    /**
     * 根据给定的属性Key, 去进行属性值的获取
     *
     * * 1.优先从localProperties当中去进行获取.
     * * 2.尝试从SystemProperties系统属性当中去进行获取
     *
     * @param key key
     * @return property(or null)
     */
    @Nullable
    @JvmStatic
    fun getProperty(key: String): String? {
        var property = localProperties.getProperty(key)
        if (property == null) {
            try {
                property = System.getProperty(key)
            } catch (ex: Exception) {
                // No logger available, use System.err to print
                System.err.println("Could not retrieve system property '$key', ex: $ex")
            }
        }
        return property
    }

    /**
     * 使用编程式的方式, 去将指定的属性Key对应的flag去设置为true
     *
     * @param key key
     */
    @JvmStatic
    fun setFlag(key: String) {
        localProperties.setProperty(key, true.toString())
    }

    /**
     * 获取给定的属性的flag值
     *
     * @param key key
     * @return flag
     */
    @JvmStatic
    fun getFlag(key: String): Boolean {
        return localProperties.getProperty(key).toBoolean()
    }
}