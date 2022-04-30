package com.wanna.framework.core.io.support

import com.wanna.framework.core.util.ClassUtils
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.util.Properties

/**
 * 这是一个Properties的Loader的工具类，可以完成Properties的加载
 */
class PropertiesLoaderUtils {
    companion object {
        /**
         * 完成Properties的属性的加载
         *
         * @param classpath 类路径
         */
        @JvmStatic
        fun loadProperties(classpath: String): Properties {
            val properties = Properties()
            try {
                val classLoader = ClassUtils.getDefaultClassLoader()
                val resources = classLoader.getResources(classpath)
                while (resources.hasMoreElements()) {
                    val element = resources.nextElement()
                    properties.load(element.openStream())
                }
            } catch (ex: Exception) {
                throw FileNotFoundException("无法加载Properties资源文件，路径为[$classpath]")
            }
            return properties
        }
    }
}