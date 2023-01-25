package com.wanna.framework.core.io.support

import com.wanna.common.logging.LoggerFactory
import com.wanna.framework.core.io.*
import com.wanna.framework.core.io.support.ResourcePatternResolver.Companion.CLASSPATH_ALL_URL_PREFIX
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.*
import com.wanna.framework.util.PathMatcher
import com.wanna.framework.util.ResourceUtils.FILE_URL_PREFIX
import com.wanna.framework.util.ResourceUtils.JAR_URL_PREFIX
import com.wanna.framework.util.ResourceUtils.JAR_URL_SEPARATOR
import com.wanna.framework.util.ResourceUtils.URL_PROTOCOL_JAR
import com.wanna.framework.util.ResourceUtils.WAR_URL_SEPARATOR
import java.io.File
import java.io.IOException
import java.net.*
import java.nio.file.*
import java.util.function.Predicate
import java.util.jar.JarFile
import java.util.stream.Stream
import java.util.zip.ZipException

/**
 * 支持路径表达式的[Resource]的解析的[ResourceLoader], 支持去处理类似"classpath:/**/xxx.xml"这样的表达式,
 * 支持在路径当中写"*"/"**"等格式的通配符, 也就是支持使用Ant表达式的方式去对资源去进行匹配
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/2
 * @param resourceLoader 执行单个资源加载的ResourceLoader, 默认情况下使用的就是DefaultResourceLoader
 *
 * @see ResourcePatternResolver
 * @see ResourceLoader
 * @see PathMatcher
 * @see AntPathMatcher
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
         * 获取到Java的ClassPath的系统属性名
         */
        private const val JAVA_CLASS_PATH_PROPERTY_NAME = "java.class.path"

        /**
         * 路径的分隔符的系统属性名
         */
        private const val PATH_SEPARATOR = "path.separator"


        /**
         * 如果给定的path以"/"作为开头的话, 那么需要去掉path开头的"/"
         *
         * @param path path
         * @return 如果path以"/"作为开头, 需要去掉"/"; 否则直接return path即可
         */
        @JvmStatic
        private fun stripLeadingSlash(path: String): String {
            return if (path.startsWith('/')) path.substring(1) else path
        }
    }

    /**
     * 提供资源路径的匹配的Ant表达式的匹配器
     */
    private var pathMatcher: PathMatcher = AntPathMatcher()


    /**
     * 提供一个基于ClassLoader去进行构建的的构造器
     *
     * @param classLoader ClassLoader
     */
    constructor(classLoader: ClassLoader) : this(DefaultResourceLoader(classLoader))


    /**
     * 根据没有表达式的直接路径, 去进行[Resource]的获取
     *
     * @param location 资源路径
     * @return 根据给定的资源路径, 去解析到的[Resource]
     */
    override fun getResource(location: String): Resource = resourceLoader.getResource(location)

    /**
     * 获取到当前[ResourceLoader]进行[Resource]的加载时所使用到的ClassLoader
     *
     * @return ClassLoader to use
     */
    @Nullable
    override fun getClassLoader(): ClassLoader? = resourceLoader.getClassLoader()

    /**
     * 获取[PathMatcher], 去对路径去进行匹配
     *
     * @return PathMatcher
     */
    open fun getPathMatcher(): PathMatcher = this.pathMatcher

    /**
     * 自定义[PathMatcher], 提供资源表达式的解析工作
     *
     * @param pathMatcher PathMatcher
     */
    open fun setPathMatcher(pathMatcher: PathMatcher) {
        this.pathMatcher = pathMatcher
    }

    /**
     * 根据给定的资源路径的Ant表达式, 去执行资源的解析
     *
     * @param locationPattern 资源路径的Ant表达式
     * @return 根据给定的资源表达式, 去解析到的资源文件列表
     */
    override fun getResources(locationPattern: String): Array<Resource> {
        // 如果给定的locationPattern是以"classpath*:"开头的话, 说明需要去进行多个Resource的加载...
        if (locationPattern.startsWith(CLASSPATH_ALL_URL_PREFIX)) {

            // 去掉开头的"classpath*:"
            val locationPatternWithoutPrefix =
                locationPattern.substring(CLASSPATH_ALL_URL_PREFIX.length)

            // 如果给定的locationPattern当中含有表达式的话, 那么走带表达式的匹配的逻辑
            if (getPathMatcher().isPattern(locationPatternWithoutPrefix)) {

                // 根据含有"classpath*:"的原始的表达式去进行表达式匹配
                return findPathMatchingResources(locationPattern)

                // 如果locationPattern当中不包含表达式的话, 那么直接通过ClassLoader.getResources去进行获取
            } else {
                return findAllClassPathResources(locationPatternWithoutPrefix)
            }
        } else {
            // 如果是Tomcat的"war:"的locationPattern, 那么需要切取"*/"之后的部分去判断是否存在有表达式
            // 否则的话, 把protocol(例如"classpath"/"file")去掉就行了
            val prefixEnd =
                if (locationPattern.startsWith(WAR_URL_SEPARATOR)) locationPattern.indexOf(WAR_URL_SEPARATOR) + 1
                else locationPattern.indexOf(":") + 1

            // 如果不是以"classpath*"开头, 但是也是含有表达式的话, 那么需要根据表达式寻找到和表达式匹配的单个Resource...
            if (getPathMatcher().isPattern(locationPattern.substring(prefixEnd))) {

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
     * @param locationPattern 资源路径表达式(Ant表达式)
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

        // trace 根据资源表达式去解析得到的资源列表信息...
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
     * @param subPattern subPattern(Ant表达式)
     * @return 通过rootDir下的所有的Resource, 和给定的subPattern去进行匹配, 最终得到的资源列表
     */
    protected open fun doFindPathMatchingJarResources(
        rootDirResource: Resource,
        rootDirUrl: URL,
        subPattern: String
    ): Set<Resource> {
        val connection = rootDirUrl.openConnection()


        val jarFile: JarFile  // Jar包对应的JarFile对象
        val jarFileUrl: String  // Jar包所处的路径url
        var rootEntryPath: String  // rootDir相比Jar包的根路径的偏移路径

        var closeJarFile = true
        // 如果该Connection是一个JarURLConnection, 那么我们可以直接通过它去获取到
        // 该rootDir所在的JarFile, 以及rootDir所在的JarFileEntry...
        if (connection is JarURLConnection) {
            jarFile = connection.jarFile
            jarFileUrl = connection.jarFileURL.toExternalForm()
            val jarEntry = connection.jarEntry
            rootEntryPath = jarEntry?.name ?: ""

            closeJarFile = !connection.useCaches
        } else {
            // 如果Connection并不是一个JarURLConnection的话...
            // 我们直接假设URL都是以"jar:path!/entry"(或"war:path*/entry")的方式去进行格式化的
            val urlFile = rootDirUrl.file
            try {
                var separatorIndex = urlFile.indexOf(WAR_URL_SEPARATOR)
                if (separatorIndex == -1) {
                    separatorIndex = urlFile.indexOf(JAR_URL_SEPARATOR)
                }
                if (separatorIndex != -1) {
                    jarFileUrl = urlFile.substring(0, separatorIndex)
                    rootEntryPath = urlFile.substring(separatorIndex + 2)  // 不管是jar/war的分隔符, 长度都为2
                    jarFile = getJarFile(jarFileUrl)
                } else {
                    jarFile = JarFile(urlFile)
                    jarFileUrl = urlFile
                    rootEntryPath = ""
                }

            } catch (ex: ZipException) {
                // 如果构建JarFile出现了问题, skip掉...
                if (logger.isDebugEnabled) {
                    logger.debug("Skipping invalid jar classpath entry [$urlFile]", ex)
                }
                return emptySet()
            }
        }

        try {
            // trace记录一下, 在哪个Jar包当中去进行Resource的搜索?
            if (logger.isTraceEnabled) {
                logger.trace("Looking for matching resources in jar file [$jarFileUrl]")
            }

            // 如果rootEntryPath的末尾不含有"/"的话, 那么在这去补充一个"/"(对于sun包的JAR不会返回"/", 对于JRockit的JRE会返回"/")
            if (StringUtils.hasText(rootEntryPath) && !rootEntryPath.endsWith("/")) {
                rootEntryPath += "/"
            }

            val result = LinkedHashSet<Resource>(8)

            // 列举出来该Jar包当中的所有的JarEntry
            for (jarEntry in jarFile.entries().asIterator()) {
                // 获取到当前Entry所在的路径
                val entryPath = jarEntry.name

                // 如果当前entryPath确实是以我们想要的rootEntryPath作为前缀, 那么我们才需要去进行处理
                if (entryPath.startsWith(rootEntryPath)) {

                    // 把前缀去掉, 只要后缀, 使用后缀, 让它去和subPattern去进行匹配...
                    val relativePath = entryPath.substring(rootEntryPath.length)
                    if (getPathMatcher().match(subPattern, relativePath)) {
                        result += rootDirResource.createRelative(relativePath)
                    }
                }
            }
            return result
        } finally {
            // 如果不是走的JarFile的缓存的话, 那么需要在这里去关闭JarFile
            if (closeJarFile) {
                jarFile.close()
            }
        }
    }

    /**
     * 列举出来rootDir下的所有的Resource文件, 分别去执行对于文件资源的路径表达式匹配,
     * 并返回和给定的subPattern完全匹配的那些Resource
     *
     * @param rootDirResource rootDirResource
     * @param subPattern subPattern(Ant表达式)
     * @return 解析表达式最终得到的资源列表
     *
     * @see Files.walk
     * @see PathMatcher.match
     */
    protected open fun doFindPathMatchingFileResources(rootDirResource: Resource, subPattern: String): Set<Resource> {
        // 获取到rootDirResource的URI
        val rootDirUri: URI
        try {
            rootDirUri = rootDirResource.getURI()
        } catch (ex: Exception) {
            // 如果因为异常获取不到就算了...
            if (logger.isInfoEnabled) {
                logger.info("Failed to resolve $rootDirResource as URI", ex)
            }
            return emptySet()
        }

        // 如果rootDirUri是绝对路径的话, 那么直接使用它作为Path
        var rootPath: Path? = null
        if (rootDirUri.isAbsolute && !rootDirUri.isOpaque) {
            try {
                try {
                    rootPath = Path.of(rootDirUri)
                } catch (ex: FileSystemNotFoundException) {
                    // 如果文件系统没有找到的话, 那么尝试利用它去创建一个新的文件系统???
                    FileSystems.newFileSystem(rootDirUri, emptyMap<String, Any>(), ClassUtils.getDefaultClassLoader())

                    // 创建完成文件系统之后, 重新尝试获取rootPath
                    rootPath = Path.of(rootDirUri)
                }
            } catch (ex: Exception) {
                // 如果获取rootPath失败, 那么打个debug log, fallback, 尝试使用Resource.getFile去作为路径
                if (logger.isDebugEnabled) {
                    logger.debug("Failed to resolve $rootDirUri in file system", ex)
                }
            }
        }

        // 如果从URI当中不是绝对路径的话, 那么尝试从rootDirResource的File当中去进行获取Path作为fallback...
        if (rootPath == null) {
            rootPath = Path.of(rootDirResource.getFile().path)
        }

        // 清理一下path, 并保证它末尾有"/", 因为它必须得是一个文件夹
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

        // trace记录一下在某个文件夹下去搜索表达式匹配的文件...
        if (logger.isTraceEnabled) {
            logger.trace("Searching directory [${rootPath!!.toAbsolutePath()}] for files matching pattern [$resourcePattern]")
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
            // 如果搜索某些文件失败的话, 那么就直接返回了, 剩下的不去搜索了...
            if (logger.isDebugEnabled) {
                logger.debug(
                    "Failed to complete search in directory [${rootPath!!.toAbsolutePath()}] for files matching pattern [$subPattern]",
                    ex
                )
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
     * 根据给定的JarFileUrl, 去获取到[JarFile]对象
     *
     * @param jarFileUrl jarFileUrl
     * @return 解析得到的[JarFile]对象
     */
    protected open fun getJarFile(jarFileUrl: String): JarFile {
        // 如果url以"file:"开头, 那么按照文件的方式去进行解析
        if (jarFileUrl.startsWith(FILE_URL_PREFIX)) {
            try {
                return JarFile(ResourceUtils.toURI(jarFileUrl).schemeSpecificPart)
            } catch (ex: Exception) {
                return JarFile(jarFileUrl.substring(FILE_URL_PREFIX.length))
            }
        }
        // 如果url不是以"file:"开头, 那么
        return JarFile(jarFileUrl)
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
        // 对于location可以使用"/META-INF/"这样的格式, 这里手动给去掉开头的"/"就行...
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

        // 如果path为""的话, 那么上面的结果看起来并不完整, 还需要添加ClassPath的根路径
        if (!StringUtils.hasText(path)) {
            addAllClassLoaderJarRoots(classLoader, result)
        }

        return result
    }

    /**
     * 添加所有的ClassLoader的Jar包根目录到result列表当中
     *
     * @param classLoader ClassLoader
     * @param result Resource结果列表
     */
    protected open fun addAllClassLoaderJarRoots(@Nullable classLoader: ClassLoader?, result: MutableSet<Resource>) {
        // 如果是URLClassLoader, 那么我们将它的所有URL去转换成为Resource
        if (classLoader is URLClassLoader) {
            try {
                for (url in classLoader.urLs) {
                    try {
                        val jarResource: UrlResource
                        if (url.protocol == URL_PROTOCOL_JAR) {
                            jarResource = UrlResource(url)
                        } else {
                            jarResource =
                                UrlResource(JAR_URL_PREFIX + url + JAR_URL_SEPARATOR)
                        }

                        // 如果存在的话, 那么收集到result当中...
                        if (jarResource.exists()) {
                            result += jarResource
                        }
                    } catch (ex: MalformedURLException) {
                        // 如果遇到了URL相关异常的话...skip
                        if (logger.isDebugEnabled) {
                            logger.debug(
                                "Cannot search for matching files underneath [$url] because it cannot be converted to a valid 'jar:' URL: ${ex.message}",
                                ex
                            )
                        }
                    }
                }

            } catch (ex: Exception) {
                // 如果该ClassLoader不支持使用getURLs方法的话...skip
                if (logger.isDebugEnabled) {
                    logger.debug(
                        "Cannot introspect jar files since ClassLoader [$classLoader] does not support 'getURLs()'",
                        ex
                    )
                }
            }
        }

        // 如果是SystemClassLoader(AppClassLoader), 那么需要添加Manifest("java.class.path")当中的Entry到结果当中
        if (classLoader == ClassLoader.getSystemClassLoader()) {
            addClassPathManifestEntries(result)
        }


        // 尝试从它的parent ClassLoader去进行寻找
        if (classLoader != null) {
            try {
                addAllClassLoaderJarRoots(classLoader.parent, result)
            } catch (ex: Exception) {
                // 有可能ClassLoader不支持getParent, 那么pass掉就行...
                if (logger.isDebugEnabled) {
                    logger.debug(
                        "Cannot introspect jar files in parent ClassLoader since [$classLoader] does not support 'getParent()",
                        ex
                    )
                }
            }
        }
    }

    /**
     * 在"java.class.path"这个Manifest的系统属性当中给定的Jar包路径,
     * 并将这些Jar包转换成为[Resource]收集到result列表当中
     *
     * @param result 用于收集最终的Resource结果的列表
     */
    protected open fun addClassPathManifestEntries(result: MutableSet<Resource>) {
        try {
            val javaClassPath = System.getProperty(JAVA_CLASS_PATH_PROPERTY_NAME, "")
            val pathSeparator = System.getProperty(PATH_SEPARATOR)
            for (path in javaClassPath.split(pathSeparator)) {
                try {
                    var filePath = File(path).absolutePath
                    if (filePath.indexOf(':') == 1) {
                        // 可能是Windows的盘符("C:"), 需要将首字母小写, 用于去进行去重的检查
                        filePath = StringUtils.capitalize(filePath)
                    }

                    // '#'可能出现在目录/文件名当中, 但是java.net.URL不应该把它当做一个fragment
                    filePath = StringUtils.replace(filePath, "#", "%23")

                    // 手动去构建一个Jar包的路径("jar:file:{filePath}!/")作为urlResource
                    val urlResource = UrlResource(JAR_URL_PREFIX + FILE_URL_PREFIX + filePath + JAR_URL_SEPARATOR)

                    // 很可能在URLClassLoader.getURLs当中已经添加过了, 因此这里需要去进行去重的检查...
                    if (!result.contains(urlResource) && !hasDuplicate(filePath, result) && urlResource.exists()) {
                        result += urlResource
                    }
                } catch (ex: MalformedURLException) {
                    if (logger.isDebugEnabled) {
                        logger.debug("Cannot search for matching files underneath [$path] because it cannot be converted to a valid 'jar:' URL: ${ex.message}")
                    }
                }
            }
        } catch (ex: Exception) {
            if (logger.isDebugEnabled) {
                logger.debug("Failed to evaluate 'java.class.path' manifest entries", ex)
            }
        }
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

    /**
     * 检查给定的filePath的异构的情况, 在result当中是否已经存在有该[Resource]?
     *
     * @param filePath filePath(可能以"/"开头, 也可以不以"/"开头)
     * @param result Resource结果列表
     * @return 如果result列表已经存在有filePath对应的Resource的话, 那么return true; 否则return false
     */
    private fun hasDuplicate(filePath: String, result: MutableSet<Resource>): Boolean {
        if (result.isEmpty()) {
            return false
        }
        val duplicatePath = if (filePath.startsWith("/")) filePath.substring(1) else "/$filePath"
        return try {
            result.contains(UrlResource(JAR_URL_PREFIX + FILE_URL_PREFIX + duplicatePath + JAR_URL_SEPARATOR))
        } catch (ex: Exception) {
            // 出现异常就pass, 我们只是进行重复的检查罢了...
            false
        }
    }
}