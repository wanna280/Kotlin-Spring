package com.wanna.logger.impl.utils

import java.io.FileNotFoundException
import java.io.InputStream
import java.util.*

/**
 * 这是一个Properties的Loader的工具类, 可以完成Properties的加载
 */
object PropertiesUtils {
    /**
     * 使用默认的classLoader去完成Properties的属性信息的加载
     *
     * @param classpath 类路径
     */
    @JvmStatic
    fun loadProperties(classpath: String): Properties {
        return loadProperties(classpath, PropertiesUtils::class.java.classLoader)
    }

    /**
     * 使用指定classLoader去完成Properties的属性信息的加载
     *
     * @param classpath 类路径
     * @param classLoader classLoader(如果为空, 使用默认的classLoader)
     */
    @JvmStatic
    fun loadProperties(classpath: String, classLoader: ClassLoader?): Properties {
        val properties = Properties()
        try {
            val classLoaderToUse = classLoader ?: PropertiesUtils::class.java.classLoader
            val resources = classLoaderToUse.getResources(classpath)
            while (resources.hasMoreElements()) {
                val element = resources.nextElement()
                properties.load(element.openStream())
            }
        } catch (ex: Exception) {
            throw FileNotFoundException("无法加载Properties资源文件, 路径为[$classpath]")
        }
        return properties
    }

    /**
     * 完成Properties的属性的加载
     */
    @JvmStatic
    fun loadProperties(inputStream: InputStream): Properties {
        val properties = Properties()
        properties.load(inputStream)
        return properties
    }
}