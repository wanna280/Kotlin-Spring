package com.wanna.framework.core.io.support

import com.wanna.framework.core.io.Resource
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * 这是一个Properties的Loader的工具类, 可以完成Properties的加载
 *
 * @see SpringFactoriesLoader
 */
object PropertiesLoaderUtils {

    private const val XML_EXTENSION = ".xml"

    private const val PROPERTIES_EXTENSION = ".properties"

    /**
     * 使用默认的classLoader去完成Properties的属性信息的加载
     *
     * @param classpath 类路径
     */
    @JvmStatic
    @Deprecated("使用loadAllProperties代替", replaceWith = ReplaceWith("loadAllProperties(classpath)"))
    fun loadProperties(classpath: String): Properties {
        return loadProperties(classpath, PropertiesLoaderUtils::class.java.classLoader)
    }

    /**
     * 将指定的资源去读取成为Properties
     *
     * @param resource resource
     */
    @JvmStatic
    fun loadProperties(resource: Resource): Properties {
        val properties = Properties()
        fillProperties(properties, resource)
        return properties
    }

    /**
     * 填充Properties, 将给定的Resource当中的内容填充到Properties当中
     *
     * @param properties 待填充的Properties
     * @param resource 资源(可以是XML文件, 也可以是Properties文件)
     * @throws IOException 当出现IO错误的情况
     */
    @Throws(IOException::class)
    @JvmStatic
    fun fillProperties(properties: Properties, resource: Resource) {
        resource.getInputStream().use {
            val filename = resource.getFilename()
            if (filename != null && filename.endsWith(XML_EXTENSION)) {
                properties.loadFromXML(it)
            } else {
                properties.load(it)
            }
        }
    }


    /**
     * 使用指定指定的ClassLoader去完成Properties的属性信息的加载
     *
     * @param classpath 需要去进行加载属性的资源类路径
     * @param classLoader classLoader(如果为空, 使用默认的classLoader)
     */
    @JvmStatic
    @Deprecated("使用loadAllProperties代替", replaceWith = ReplaceWith("loadAllProperties"))
    fun loadProperties(classpath: String, classLoader: ClassLoader?): Properties {
        return loadAllProperties(classpath, classLoader)
    }

    /**
     * 使用指定指定的ClassLoader去完成Properties的属性信息的加载
     *
     * @param classpath 需要去进行加载属性的资源类路径
     * @return 加载到的Properties
     */
    @JvmStatic
    fun loadAllProperties(classpath: String): Properties =
        loadAllProperties(classpath, PropertiesLoaderUtils::class.java.classLoader)

    /**
     * 使用指定指定的ClassLoader去完成Properties的属性信息的加载
     *
     * @param classpath 需要去进行加载属性的资源类路径
     * @param classLoader classLoader(如果为空, 使用默认的classLoader)
     * @return 加载到的Properties
     */
    @JvmStatic
    fun loadAllProperties(classpath: String, classLoader: ClassLoader?): Properties {
        val properties = Properties()
        try {
            val classLoaderToUse = classLoader ?: PropertiesLoaderUtils::class.java.classLoader
            val resources = classLoaderToUse.getResources(classpath)
            while (resources.hasMoreElements()) {
                val element = resources.nextElement()
                properties += if (classpath.endsWith(XML_EXTENSION)) {
                    loadPropertiesFromXml(element.openStream())
                } else if (classpath.endsWith(PROPERTIES_EXTENSION)) {
                    loadPropertiesFromProperties(element.openStream())
                } else {
                    throw IllegalStateException("不支持处理该类型的文件[$classpath]")
                }
            }
        } catch (ex: IOException) {
            throw FileNotFoundException("无法加载Properties资源文件, 路径为[$classpath]")
        }
        return properties
    }

    /**
     * 从".properties"文件当中去加载Properties
     *
     * @param inputStream Properties文件的路径
     * @return 加载完成的Properties
     */
    @JvmStatic
    fun loadPropertiesFromProperties(inputStream: InputStream): Properties {
        val properties = Properties()
        inputStream.use { properties.load(it) }  // use method for close io stream automatically
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
        inputStream.use { properties.loadFromXML(it) }  // use method for close io stream automatically
        return properties
    }
}