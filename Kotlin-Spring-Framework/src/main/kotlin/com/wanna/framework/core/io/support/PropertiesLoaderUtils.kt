package com.wanna.framework.core.io.support

import java.io.FileNotFoundException
import java.io.InputStream
import java.util.*

/**
 * 这是一个Properties的Loader的工具类，可以完成Properties的加载
 */
object PropertiesLoaderUtils {

    const val XML_EXTENSION = ".xml"

    /**
     * 使用默认的classLoader去完成Properties的属性信息的加载
     *
     * @param classpath 类路径
     */
    @JvmStatic
    fun loadProperties(classpath: String): Properties {
        return loadProperties(classpath, PropertiesLoaderUtils::class.java.classLoader)
    }

    /**
     * 使用指定classLoader去完成Properties的属性信息的加载
     *
     * @param classpath 类路径
     * @param classLoader classLoader(如果为空，使用默认的classLoader)
     */
    @JvmStatic
    fun loadProperties(classpath: String, classLoader: ClassLoader?): Properties {
        val properties = Properties()
        try {
            val classLoaderToUse = classLoader ?: PropertiesLoaderUtils::class.java.classLoader
            val resources = classLoaderToUse.getResources(classpath)
            while (resources.hasMoreElements()) {
                val element = resources.nextElement()
                if (classpath.endsWith(XML_EXTENSION)) {
                    properties += loadPropertiesFromXml(element.openStream())
                } else {
                    properties += loadPropertiesFromProperties(element.openStream())
                }
            }
        } catch (ex: Exception) {
            throw FileNotFoundException("无法加载Properties资源文件，路径为[$classpath]")
        }
        return properties
    }

    /**
     * 从Properties文件当中去加载Properties
     *
     * @param inputStream Properties文件的路径
     * @return 加载完成的Proerties
     */
    @JvmStatic
    fun loadPropertiesFromProperties(inputStream: InputStream): Properties {
        val properties = Properties()
        properties.load(inputStream)
        return properties
    }

    /**
     * 从XML配置文件当中去加载Properties
     *
     * @param inputStream Xml文件的输入流
     * @return 加载完成的的Properties
     */
    private fun loadPropertiesFromXml(inputStream: InputStream): Properties {
        val properties = Properties()
        properties.loadFromXML(inputStream)
        return properties
    }
}