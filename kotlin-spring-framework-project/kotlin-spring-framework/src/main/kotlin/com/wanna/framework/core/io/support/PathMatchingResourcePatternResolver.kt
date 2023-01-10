package com.wanna.framework.core.io.support

import com.wanna.framework.core.io.DefaultResourceLoader
import com.wanna.framework.core.io.Resource
import com.wanna.framework.core.io.ResourceLoader
import com.wanna.framework.core.io.UrlResource
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.AntPathMatcher
import com.wanna.framework.util.PathMatcher
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.URL
import kotlin.jvm.Throws

/**
 * 支持表达式解析的ResourceLoader
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/2
 * @param resourceLoader 执行资源加载的ResourceLoader, 默认情况下使用的就是DefaultResourceLoader
 */
open class PathMatchingResourcePatternResolver(val resourceLoader: ResourceLoader = DefaultResourceLoader()) :
    ResourcePatternResolver {

    companion object {
        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(PathMatchingResourcePatternResolver::class.java)

        /**
         * 去掉path开头的'/'
         *
         * @param path path
         * @return 如果path以"/"作为开头, 需要去掉"/"; 否则直接return path即可
         */
        @JvmStatic
        private fun stripLeadingSlash(path: String): String {
            return if (path.startsWith('/')) path.substring(0) else path
        }
    }

    private val pathMatcher: PathMatcher = AntPathMatcher()


    /**
     * 提供一个基于ClassLoader去进行构建的的构造器
     *
     * @param classLoader ClassLoader
     */
    constructor(classLoader: ClassLoader) : this(DefaultResourceLoader(classLoader))


    override fun getResource(location: String) = resourceLoader.getResource(location)

    @Nullable
    override fun getClassLoader(): ClassLoader? = resourceLoader.getClassLoader()

    override fun getResources(locationPattern: String): Array<Resource> {
        return getResources0(locationPattern)
    }

    /**
     * TODO
     */
    private fun getResource1(locationPattern: String): Array<Resource> {
        // 如果以"classpath*"开头的话, 说明需要去进行多个Resource的加载...
        if (locationPattern.startsWith(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX)) {
            val locationPatternWithoutPrefix =
                locationPattern.substring(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX.length)
            if (pathMatcher.isPattern(locationPatternWithoutPrefix)) {
                return findPathMatchingResources(locationPatternWithoutPrefix)
            } else {
                return findAllClassPathResources(locationPattern)
            }
        } else {

            // 如果含有表达式的话, 那么需要寻找路径匹配的Resource...
            if (pathMatcher.isPattern(locationPattern)) {

                return findPathMatchingResources(locationPattern)
                // 如果没有表达式的话, 那么直接根据Location去加载Resource
            } else {
                return arrayOf(resourceLoader.getResource(locationPattern))
            }
        }
    }

    private fun findPathMatchingResources(locationPattern: String): Array<Resource> {
        return emptyArray()
    }

    /**
     * 根据给定的直接classpath路径, 去找到所有条件符合的[Resource]
     * (其实就是[ClassLoader.getResources]去获取到资源的URL的迭代器, 并包装成为[Resource])
     *
     * @param location location
     * @return 根据该Location去寻找到的所有的Resource
     * @see ClassLoader.getResources
     */
    @Throws(IOException::class)
    protected open fun findAllClassPathResources(location: String): Array<Resource> {
        // 去掉路径开头的"/"
        val path = stripLeadingSlash(location)

        // 使用ClassLoader.getResources去解析到对应的资源...
        val resources = doFindAllClassPathResources(path)
        if (logger.isTraceEnabled) {
            logger.trace("Resolved classpath location [$path] to resources $resources")
        }
        return resources.toTypedArray()
    }

    /**
     * 寻找到所有的ClassPath的资源
     *
     * @param path path
     * @return 根据给定的path去加载得到的所有合适的Resource
     */
    protected open fun doFindAllClassPathResources(path: String): Set<Resource> {
        val result = LinkedHashSet<Resource>()

        // 使用ClassLoader去获取到所有的资源的URL, 去转换成为Resource
        val classLoader = getClassLoader() ?: ClassLoader.getSystemClassLoader()
        val resources = classLoader.getResources(path)
        while (resources.hasMoreElements()) {
            val url = resources.nextElement()
            result.add(convertClassLoaderURL(url))
        }
        // TODO, no path?

        return result
    }

    /**
     * 根据给定的URL, 去创建出来一个Resource, 默认实现是去创建一个[UrlResource]
     *
     * @param url URL
     * @return 根据Resource创建出来的Resource
     */
    protected open fun convertClassLoaderURL(url: URL): Resource {
        return UrlResource(url)
    }

    /**
     * 目前不会做表达式的解析, 因此我们目前使用的是基于Spring的实现
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