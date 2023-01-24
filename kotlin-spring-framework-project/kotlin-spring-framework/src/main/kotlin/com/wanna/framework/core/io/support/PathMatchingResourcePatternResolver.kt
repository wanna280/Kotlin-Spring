package com.wanna.framework.core.io.support

import com.wanna.common.logging.LoggerFactory
import com.wanna.framework.core.io.*
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.AntPathMatcher
import com.wanna.framework.util.PathMatcher
import com.wanna.framework.util.ResourceUtils
import com.wanna.framework.util.StringUtils
import java.io.IOException
import java.net.JarURLConnection
import java.net.URI
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.function.Predicate
import java.util.jar.JarFile
import java.util.stream.Stream

/**
 * 支持路径表达式的[Resource]的解析的[ResourceLoader], 支持去处理类似"classpath:/**/xxx.xml"这样的表达式,
 * 支持在路径当中写"*"/"**"等格式的通配符
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/2
 * @param resourceLoader 执行单个资源加载的ResourceLoader, 默认情况下使用的就是DefaultResourceLoader
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

    /**
     * 获取[PathMatcher], 去对路径去进行匹配
     *
     * @return PathMatcher
     */
    open fun getPathMatcher(): PathMatcher = this.pathMatcher

    override fun getResources(locationPattern: String): Array<Resource> {
        return getResources1(locationPattern)
    }

    /**
     * TODO
     */
    private fun getResources1(locationPattern: String): Array<Resource> {
        // 如果以"classpath*"开头的话, 说明需要去进行多个Resource的加载...
        if (locationPattern.startsWith(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX)) {
            val locationPatternWithoutPrefix =
                locationPattern.substring(ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX.length)

            // 如果路径当中含有表达式的话, 那么走带表达式的匹配的逻辑
            if (getPathMatcher().isPattern(locationPatternWithoutPrefix)) {

                // 根据含有"classpath*"的表达式去进行匹配
                return findPathMatchingResources(locationPattern)

                // 如果路径当中不包含表达式的话, 那么直接通过ClassLoader.getResources去进行获取
            } else {
                return findAllClassPathResources(locationPatternWithoutPrefix)
            }
        } else {

            // 如果含有表达式的话, 那么需要寻找路径匹配的Resource...
            if (getPathMatcher().isPattern(locationPattern)) {

                return findPathMatchingResources(locationPattern)
                // 如果没有表达式的话, 那么直接根据Location去加载Resource
            } else {
                return arrayOf(resourceLoader.getResource(locationPattern))
            }
        }
    }

    /**
     * 将含有表达式的路径匹配的路径表达式, 和候选的所有的资源信息, 去进行匹配
     *
     * @param locationPattern 资源路径表达式
     * @return 根据该表达式去解析得到的Resource
     */
    private fun findPathMatchingResources(locationPattern: String): Array<Resource> {
        // 把rootDir和subDir拆分开, 例如"/WEB-INF/*.xml", rootDir="/WEB-INF/", subDir="*.xml"
        val rootDir = determineRootDir(locationPattern)
        val subPattern = locationPattern.substring(rootDir.length)

        // 把rootDir, 交给getResources去进行递归寻找...
        val rootDirResources = getResources(rootDir)
        val result = LinkedHashSet<Resource>()

        // 遍历所有的rootDirResource, 以这些位置作为root去进行资源的寻找
        for (rootDirResource in rootDirResources) {
            val rootDirUrl = rootDirResource.getURL()

            // 如果是Jar的Resource的话...
            if (ResourceUtils.isJarURL(rootDirUrl) || isJarResource(rootDirResource)) {
                result += doFindPathMatchingJarResources(rootDirResource, rootDirUrl, subPattern)
                // 如果是File的Resource的话...
            } else {
                result += doFindPathMatchingFileResources(rootDirResource, subPattern)
            }
        }
        if (logger.isTraceEnabled) {
            logger.trace("Resolved location pattern [$locationPattern] to resources $result")
        }
        return result.toTypedArray()
    }

    /**
     * 列举出来Jar包当中的rootDir下的所有Resource, 分别去对ClassPath的路径表达式去进行匹配
     *
     * @param rootDirResource rootDirResource
     * @param rootDirUrl rootDirUrl
     * @param subPattern subPattern
     * @return 解析表达式最终得到的资源列表
     */
    protected open fun doFindPathMatchingJarResources(
        rootDirResource: Resource,
        rootDirUrl: URL,
        subPattern: String
    ): Set<Resource> {
        val connection = rootDirUrl.openConnection()
        var jarFile: JarFile? = null
        var jarFileUrl: String? = null
        var rootEntryPath: String? = null
        if (connection is JarURLConnection) {
            jarFile = connection.jarFile
            jarFileUrl = connection.jarFileURL.toExternalForm()
            val jarEntry = connection.jarEntry
            rootEntryPath = jarEntry?.name ?: ""
        } else {
            // TODO
        }

        try {
            val result = LinkedHashSet<Resource>()

            // 列举出来该Jar包当中的所有的JarEntry
            for (jarEntry in jarFile!!.entries().asIterator()) {
                // 获取到当前Entry所在的路径
                val entryPath = jarEntry.name

                // 如果当前entryPath确实是以我们想要的rootEntryPath作为前缀, 那么我们才需要去进行处理
                if (entryPath.startsWith(rootEntryPath!!)) {

                    // 把前缀去掉, 只要后缀, 使用后缀, 让它去和subPattern去进行匹配...
                    val relativePath = entryPath.substring(rootEntryPath.length)
                    if (getPathMatcher().match(subPattern, relativePath)) {
                        result += rootDirResource.createRelative(relativePath)
                    }
                }
            }
            return result
        } catch (ex: Exception) {
            ex.printStackTrace()
            // TODO
        }
        return emptySet()
    }

    /**
     * 列举出来rootDir下的所有的Resource, 分别去执行对于文件资源的路径表达式匹配 (TODO 异常完善, Resource完善)
     *
     * @param rootDirResource rootDirResource
     * @param subPattern subPattern
     * @return 解析表达式最终得到的资源列表
     * @see Files.walk
     */
    protected open fun doFindPathMatchingFileResources(rootDirResource: Resource, subPattern: String): Set<Resource> {
        val rootDirUri: URI
        try {
            rootDirUri = rootDirResource.getURI()
        } catch (ex: Exception) {
            return emptySet()
        }
        var rootPath: Path? = null
        if (rootDirUri.isAbsolute && !rootDirUri.isOpaque) {
            try {
                rootPath = Path.of(rootDirUri)
            } catch (ex: Exception) {
                // TODO
            }
        }
        if (rootPath == null) {
            rootPath = Path.of(rootDirResource.getFile().path)
        }

        var rootDir = StringUtils.cleanPath(rootPath.toString())
        if (!rootDir.endsWith("/")) {
            rootDir += "/"
        }

        // 将rootDir和subPattern拼接起来, 得到resourcePattern
        val resourcePattern = rootDir + StringUtils.cleanPath(subPattern)

        // 构建出来利用PathMatcher去对path pattern和path之间去进行匹配的Predicate断言...
        val isMatchingFileFilter = Predicate<Path> { path ->
            path.toString() != resourcePattern && getPathMatcher().match(
                resourcePattern,
                StringUtils.cleanPath(path.toString())
            )
        }
        val result = LinkedHashSet<Resource>()

        // 迭代rootPath下的所有File, 利用PathMatcher对它进行匹配...
        var stream: Stream<Path>? = null
        try {
            // Files.walk这个API可以列举出来rootPath下的所有的File(文件&文件夹)
            stream = Files.walk(rootPath)
            stream.filter(isMatchingFileFilter).sorted().forEach {
                try {
                    result += FileSystemResource(it.toFile())
                } catch (ex: Exception) {
                    if (logger.isDebugEnabled) {
                        logger.debug("Failed to convert file $it to com.wanna.framework.core.io.Resource", ex)
                    }
                }
            }
        } catch (ex: Exception) {
            if (logger.isDebugEnabled) {
                logger.debug("", ex)
            }
        } finally {
            stream?.close()
        }
        return result
    }

    /**
     * 根据路径表达式, 去决策出来去进行Resource的搜索的RootDir,
     * 例如针对于/WEB-INF/ *.xml这样的表达式, 将会得到"/WEB-INF/"这样的rootDir
     *
     * @param location location
     * @return rootDir
     */
    protected open fun determineRootDir(location: String): String {
        val prefixEnd = location.indexOf(':') + 1
        var rootDirEnd = location.length
        while (rootDirEnd > prefixEnd && getPathMatcher().isPattern(location.substring(prefixEnd, rootDirEnd))) {
            rootDirEnd = location.lastIndexOf('/', rootDirEnd - 2) + 1
        }
        if (rootDirEnd == 0) {
            rootDirEnd = prefixEnd
        }
        return location.substring(0, rootDirEnd)
    }

    /**
     * 判断给定的Resource, 是否是一个Jar包的Resource?
     *
     * @param resource Resource
     * @return 如果是在Jar包内的Resource, return true; 否则return false
     */
    protected open fun isJarResource(resource: Resource): Boolean {
        return false
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
     * @return 根据URL创建出来的Resource
     */
    protected open fun convertClassLoaderURL(url: URL): Resource {
        return UrlResource(url)
    }
}