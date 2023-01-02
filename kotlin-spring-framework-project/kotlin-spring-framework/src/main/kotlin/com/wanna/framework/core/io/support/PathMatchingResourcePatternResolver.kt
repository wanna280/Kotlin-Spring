package com.wanna.framework.core.io.support

import com.wanna.framework.core.io.DefaultResourceLoader
import com.wanna.framework.core.io.Resource
import com.wanna.framework.core.io.ResourceLoader
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream
import java.net.URI
import java.net.URL

/**
 * 支持表达式解析的ResourceLoader
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/2
 * @param resourceLoader 执行资源加载的ResourceLoader，默认情况下使用的就是DefaultResourceLoader
 */
open class PathMatchingResourcePatternResolver(val resourceLoader: ResourceLoader = DefaultResourceLoader()) :
    ResourcePatternResolver {

    companion object {
        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(PathMatchingResourcePatternResolver::class.java)
    }


    /**
     * 提供一个基于ClassLoader去进行构建的的构造器
     *
     * @param classLoader ClassLoader
     */
    constructor(classLoader: ClassLoader) : this(DefaultResourceLoader(classLoader))


    override fun getResource(location: String) = resourceLoader.getResource(location)

    override fun getClassLoader(): ClassLoader? = resourceLoader.getClassLoader()

    override fun getResources(locationPattern: String): Array<Resource> {
        return getResources0(locationPattern)
    }

    /**
     * 目前不会做表达式的解析，因此我们目前使用的是基于Spring的实现
     */
    private fun getResources0(locationPattern: String): Array<Resource> {
        val resolver = org.springframework.core.io.support.PathMatchingResourcePatternResolver(getClassLoader())
        return resolver.getResources(locationPattern).map { SpringBridgedResource(it) }.toTypedArray()
    }

    /**
     * 将Spring的Resource去转接到我们自己的Resource上来
     *
     * @param resource Spring的Resource
     */
    private class SpringBridgedResource(val resource: org.springframework.core.io.Resource) : Resource {
        override fun getInputStream(): InputStream = resource.inputStream
        override fun exists() = resource.exists()
        override fun getURI(): URI = resource.uri
        override fun getURL(): URL = resource.url
        override fun getFile(): File = resource.file
        override fun lastModified(): Long = resource.lastModified()
        override fun contentLength(): Long = resource.contentLength()
        override fun createRelative(relativePath: String) = SpringBridgedResource(resource.createRelative(relativePath))
        override fun getFilename(): String? = resource.filename
        override fun getDescription(): String = resource.description
        override fun toString(): String = resource.toString()
        override fun equals(other: Any?): Boolean = resource == other
        override fun hashCode(): Int = resource.hashCode()
    }
}