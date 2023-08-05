package com.wanna.boot.loader.jar

import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.ref.SoftReference
import java.net.*
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.logging.Logger
import java.util.regex.Pattern
import javax.annotation.Nullable
import kotlin.collections.set

/**
 * 为SpringBoot的Loader提供Jar包的读取的[URLStreamHandler],
 * 对于Java原生的[URLStreamHandler]并不支持去进行嵌套的Jar包的URL的读取,
 * 但是实际上, 我们需要用到嵌套Jar包的URL的读取, 因为对于SpringBoot最终打出来的Jar包,
 * 是一个FatJar, 存在有Jar包嵌套的情况, 因此我们需要自定义[URLStreamHandler],
 * 去为嵌套的Jar包的解析提供支持, 不然对于嵌套的Jar包我们无法去进行读取
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/5
 *
 * @param jarFile 需要去提供数据的读取的JarFile
 */
class Handler @JvmOverloads constructor(private val jarFile: JarFile? = null) : URLStreamHandler() {
    companion object {

        /**
         * Jar协议常量
         */
        private const val JAR_PROTOCOL = "jar:"

        /**
         * 文件协议常量
         */
        private const val FILE_PROTOCOL = "file:"

        /**
         * Tomcat的War包的协议
         */
        private const val TOMCAT_WARFILE_PROTOCOL = "war:file:"

        /**
         * Jar包的分隔符
         */
        private const val SEPARATOR = "!/"

        @JvmStatic
        private val SEPARATOR_PATTERN = Pattern.compile(SEPARATOR, Pattern.LITERAL)

        private const val CURRENT_DIR = "/./"

        @JvmStatic
        private val CURRENT_DIR_PATTERN = Pattern.compile(CURRENT_DIR, Pattern.LITERAL)

        private const val PARENT_DIR = "/../"

        /**
         * Java当中用于去进行寻找URL Protocol Handler的系统属性Key
         */
        private const val PROTOCOL_HANDLER = "java.protocol.handler.pkgs"

        /**
         * Fallback的Java原生的Jar URL Protocol Handler
         */
        @JvmStatic
        private val FALLBACK_HANDLERS = arrayOf("sun.net.www.protocol.jar.Handler")

        /**
         * JarContextURL, 用于去捕捉原始的Jar协议的Handler,
         * 我们会去进行自定义Handler, 因此我们需要去将原始的Handler去保存下来, 通过JarContextURL的方式去保存原始的Handler
         */
        @JvmStatic
        private var jarContextUrl: URL? = null

        /**
         * RootFile的缓存, 软引用
         */
        @JvmStatic
        private var rootFileCache: SoftReference<MutableMap<File, JarFile>> = SoftReference(null)

        /**
         * 在根据[URL]去解析[JarURLConnection]时, 如果遇到找不到的情况, 是否需要去进行快速丢出来[FileNotFoundException]?
         *
         * * 1.如果设置为true, 那么将会快速丢出来[FileNotFoundException]异常
         * * 2.如果设置为false, 那么将会暂时返回一个空的[JarURLConnection]
         *
         * @param useFastConnectionExceptions 是否需要快速异常的模式?
         */
        @JvmStatic
        fun setUseFastConnectionExceptions(useFastConnectionExceptions: Boolean) =
            JarURLConnection.setUseFastExceptions(useFastConnectionExceptions)

        /**
         * 添加一个给定的JarFile当中RootFileCache当中
         *
         * @param sourceFile sourceFile(Key)
         * @param jarFile JarFile(Value)
         */
        @JvmStatic
        fun addToRootFileCache(sourceFile: File, jarFile: JarFile) {
            var cache = rootFileCache.get()
            if (cache == null) {
                cache = ConcurrentHashMap()
                rootFileCache = SoftReference(cache)
            }
            cache[sourceFile] = jarFile
        }

        /**
         * 如果可能的话, 捕捉一个可以被原来的Jar Handler去进行配置的URL;
         * 以便后续我们可以使用它去作为Fallback的上下文, 在失败时, 我们可以尝试去走fallback的打开Connection的方式;
         * 在这个方法当中, 我们仅仅只是想要知道一下最原始的URLHandler是什么, 在使用完毕之后很快我们就会将JarHandler给重设回去
         */
        @JvmStatic
        fun captureJarContextUrl() {
            // 如果之前URL还未设置过URLStreamHandlerFactory的话, 那么...
            if (canResetCachedUrlHandlers()) {

                // 清空掉SystemProperties当中的ProtocolHandler属性
                val handlers = System.getProperty(PROTOCOL_HANDLER)
                try {
                    System.clearProperty(PROTOCOL_HANDLER)
                    try {

                        // 清空URL当中已经缓存的所有的URL Handler
                        resetCachedUrlHandlers()

                        // 构建出来一个"jar:file:context.jar!/"
                        // 尝试根据这个URL, 去进行URL Handler的推断
                        jarContextUrl = URL("jar:file:context.jar!/")
                        val connection = jarContextUrl?.openConnection()

                        // 如果打开的Connection是我们自定义的JarURLConnection, 那么无需使用jarContextURL
                        if (connection is JarURLConnection) {
                            jarContextUrl = null
                        }
                    } catch (ex: Exception) {
                        // ignore
                    }
                } finally {

                    // 在处理完成之后, 我们将ProtocolHandler重新设置到SystemProperties当中
                    if (handlers == null) {
                        System.clearProperty(PROTOCOL_HANDLER)
                    } else {
                        System.setProperty(PROTOCOL_HANDLER, handlers)
                    }
                }

                // 因为在JarContextURL的构建过程当中会缓存Handler, 因此在使用完成之后, 我们将URL Handler缓存清空掉
                resetCachedUrlHandlers()
            }
        }

        /**
         * 检查我们是否可以去进行重设缓存当中的URLHandler?
         * 如果之前已经设置过非空的[URLStreamHandlerFactory]的话, 那么将不允许去进行重设, return false
         *
         * @return 如果可以去进行重设URLHandler的缓存的话, 那么return true; 否则return false
         */
        @JvmStatic
        private fun canResetCachedUrlHandlers(): Boolean {
            return try {
                resetCachedUrlHandlers()
                true
            } catch (ex: Error) {
                // catch Error throw from URL.setURLStreamHandlerFactory
                false
            }
        }

        /**
         * 如果可以的话, 那么reset已经缓存的URLStreamHandler列表
         *
         * @throws Error 如果之前URL已经设置过非空URLStreamHandlerFactory
         */
        @Throws(Error::class)
        @JvmStatic
        private fun resetCachedUrlHandlers() = URL.setURLStreamHandlerFactory(null)
    }

    /**
     * 检查所有的候选的URLStreamHandler, 去获取Fallback的URLStreamHandler
     */
    private val fallbackHandler: URLStreamHandler
        get() {
            for (handlerClassName in FALLBACK_HANDLERS) {
                try {
                    val handlerClass = Class.forName(handlerClassName)
                    handlerClass.getDeclaredConstructor().newInstance() as URLStreamHandler
                } catch (ex: Exception) {
                    // Ignore
                }
            }
            throw IllegalStateException("Unable to find fallback handler")
        }

    /**
     * 根据给定的的URL, 去开启[URLConnection], 提供对于该[URL]连接的数据访问
     *
     * @param url URL
     * @return URLConnection
     */
    @Throws(IOException::class)
    override fun openConnection(url: URL): URLConnection {

        // 如果给定的URL, 在JarFile的内部的话, 那么我们直接创建一个JarURLConnection
        return if (jarFile != null && isUrlInJarFile(url, jarFile)) {
            JarURLConnection[url, jarFile]

            // 如果给定的URL不在JarFile内部的话, 那么我们尝试根据RootJarFile去进行创建
            // 如果创建失败的话, 那么我们尝试一下fallback的URLConnection
        } else try {
            JarURLConnection[url, getRootJarFileFromUrl(url)]
        } catch (ex: Exception) {

            // 尝试使用fallback的Connection
            openFallbackConnection(url, ex)
        }
    }

    /**
     * 判断给定的url是否在目标JarFile当中, 通过前缀匹配的方式去进行检查
     *
     * @param url url
     * @param jarFile JarFile
     * @return 如果该URL在JarFile当中(根据路径的前缀去进行匹配), return true; 否则return false
     */
    @Throws(MalformedURLException::class)
    private fun isUrlInJarFile(url: URL, jarFile: JarFile): Boolean {
        // Try the path first to save building a new url string each time
        return (url.path.startsWith(jarFile.getUrl().path)
                && url.toString().startsWith(jarFile.urlString!!))
    }

    /**
     * 打开fallback的Connection
     *
     * @param url url
     * @param reason 异常原因
     * @return URLConnection
     */
    @Throws(IOException::class)
    private fun openFallbackConnection(url: URL, reason: Exception): URLConnection {
        return try {

            // 尝试打开Tomcat的War包的URLConnection
            var connection = openFallbackTomcatConnection(url)

            // 如果尝试Tomcat的War包不行的话, 那么我们尝试使用jarContextUrl去进行尝试
            connection = connection ?: openFallbackContextConnection(url)

            // 如果还不行的话, 那么我们使用fallback的Jar URLStreamHandler去进行打开
            connection ?: openFallbackHandlerConnection(url)
        } catch (ex: Exception) {
            if (reason is IOException) {
                log(false, "Unable to open fallback handler", ex)
                throw reason
            }
            log(true, "Unable to open fallback handler", ex)
            if (reason is RuntimeException) {
                throw reason
            }
            throw IllegalStateException(reason)
        }
    }

    /**
     * 尝试使用Tomcat的"jar:war:file:..."这样的URL, 这种方式允许我们使用内部的Jar的支持,
     * 而不是使用[sun.net.www.protocol.jar.URLJarFile]的逻辑去提取内部的Jar包到一个我们可以临时的文件夹
     */
    private fun openFallbackTomcatConnection(url: URL): URLConnection? {
        var file = url.file
        if (isTomcatWarUrl(file)) {
            file = file.substring(TOMCAT_WARFILE_PROTOCOL.length)
            file = file.replaceFirst("\\*/".toRegex(), "!/")
            try {
                val connection = openConnection(URL("jar:file:$file"))
                connection.getInputStream().close()
                return connection
            } catch (ex: IOException) {
                // ignore
            }
        }
        return null
    }

    /**
     * 判断给定的文件名是否是一个Tomcat的War包的URL
     *
     * @param file fileName
     * @return 如果file以"war:file:"开头的话, 说明它是一个Tomcat的War包的URL, return true; 否则return false
     */
    private fun isTomcatWarUrl(file: String): Boolean {
        if (file.startsWith(TOMCAT_WARFILE_PROTOCOL) || !file.contains("*/")) {
            try {
                val connection = URL(file).openConnection()
                if (connection.javaClass.name.startsWith("org.apache.catalina")) {
                    return true
                }
            } catch (ex: Exception) {
                // ignore
            }
        }
        return false
    }


    /**
     * 根据之前捕捉到的jarContextUrl(在替换成为我们的Handler之前, 有捕捉原始的jarContextUrl)去创建fallback URLConnection
     *
     * @param url url
     * @return URLConnection(不存在jarContextUrl, 或者打开失败, return null)
     */
    @Nullable
    private fun openFallbackContextConnection(url: URL): URLConnection? {
        try {
            if (jarContextUrl != null) {
                return URL(jarContextUrl, url.toExternalForm()).openConnection()
            }
        } catch (ex: Exception) {
            // ignore
        }
        return null
    }


    /**
     * 尝试使用反射去访问Java的默认的Jar包的[URLStreamHandler]去作为fallback的Connection
     *
     * @param url url
     * @return URLConnection
     */
    @Throws(Exception::class)
    private fun openFallbackHandlerConnection(url: URL): URLConnection {
        val fallbackHandler = fallbackHandler
        return URL(null, url.toExternalForm(), fallbackHandler).openConnection()
    }

    private fun log(warning: Boolean, message: String, cause: Exception) {
        try {
            val level = if (warning) Level.WARNING else Level.FINEST
            Logger.getLogger(javaClass.name).log(level, message, cause)
        } catch (ex: Exception) {
            if (warning) {
                System.err.println("WARNING: $message")
            }
        }
    }

    override fun parseURL(context: URL, spec: String, start: Int, limit: Int) {
        if (spec.regionMatches(0, JAR_PROTOCOL, 0, JAR_PROTOCOL.length, ignoreCase = true)) {
            setFile(context, getFileFromSpec(spec.substring(start, limit)))
        } else {
            setFile(context, getFileFromContext(context, spec.substring(start, limit)))
        }
    }

    private fun getFileFromSpec(spec: String): String {
        val separatorIndex = spec.lastIndexOf("!/")
        require(separatorIndex != -1) { "No !/ in spec '$spec'" }
        return try {
            URL(spec.substring(0, separatorIndex))
            spec
        } catch (ex: MalformedURLException) {
            throw IllegalArgumentException("Invalid spec URL '$spec'", ex)
        }
    }

    private fun getFileFromContext(context: URL, spec: String): String {
        val file = context.file
        if (spec.startsWith("/")) {
            return trimToJarRoot(file) + SEPARATOR + spec.substring(1)
        }
        if (file.endsWith("/")) {
            return file + spec
        }
        val lastSlashIndex = file.lastIndexOf('/')
        require(lastSlashIndex != -1) { "No / found in context URL's file '$file'" }
        return file.substring(0, lastSlashIndex + 1) + spec
    }

    private fun trimToJarRoot(file: String): String {
        val lastSeparatorIndex = file.lastIndexOf(SEPARATOR)
        require(lastSeparatorIndex != -1) { "No !/ found in context URL's file '$file'" }
        return file.substring(0, lastSeparatorIndex)
    }

    private fun setFile(context: URL, file: String) {
        var path = normalize(file)
        var query: String? = null
        val queryIndex = path.lastIndexOf('?')
        if (queryIndex != -1) {
            query = path.substring(queryIndex + 1)
            path = path.substring(0, queryIndex)
        }
        setURL(context, JAR_PROTOCOL, null, -1, null, null, path, query, context.ref)
    }

    private fun normalize(file: String): String {
        if (!file.contains(CURRENT_DIR) && !file.contains(PARENT_DIR)) {
            return file
        }
        val afterLastSeparatorIndex = file.lastIndexOf(SEPARATOR) + SEPARATOR.length
        var afterSeparator = file.substring(afterLastSeparatorIndex)
        afterSeparator = replaceParentDir(afterSeparator)
        afterSeparator = replaceCurrentDir(afterSeparator)
        return file.substring(0, afterLastSeparatorIndex) + afterSeparator
    }

    private fun replaceParentDir(file: String): String {
        var file = file
        var parentDirIndex: Int
        while (file.indexOf(PARENT_DIR).also { parentDirIndex = it } >= 0) {
            val precedingSlashIndex = file.lastIndexOf('/', parentDirIndex - 1)
            file = if (precedingSlashIndex >= 0) {
                file.substring(0, precedingSlashIndex) + file.substring(parentDirIndex + 3)
            } else {
                file.substring(parentDirIndex + 4)
            }
        }
        return file
    }

    private fun replaceCurrentDir(file: String): String = CURRENT_DIR_PATTERN.matcher(file).replaceAll("/")

    override fun hashCode(u: URL): Int = hashCode(u.protocol, u.file)

    private fun hashCode(protocol: String?, file: String): Int {
        var result = protocol?.hashCode() ?: 0
        val separatorIndex = file.indexOf(SEPARATOR)
        if (separatorIndex == -1) {
            return result + file.hashCode()
        }
        val source = file.substring(0, separatorIndex)
        val entry = canonicalize(file.substring(separatorIndex + 2))
        result += try {
            URL(source).hashCode()
        } catch (ex: MalformedURLException) {
            source.hashCode()
        }
        result += entry.hashCode()
        return result
    }

    /**
     * 判断给定的两个URL是否是相同的文件?
     *
     * @param u1 url1
     * @param u2 url2
     * @return 如果是同一个文件return true, 否则return false
     */
    override fun sameFile(u1: URL, u2: URL): Boolean {
        if (u1.protocol != "jar" || u2.protocol != "jar") {
            return false
        }
        val separator1 = u1.file.indexOf(SEPARATOR)
        val separator2 = u2.file.indexOf(SEPARATOR)
        if (separator1 == -1 || separator2 == -1) {
            return super.sameFile(u1, u2)
        }
        val nested1 = u1.file.substring(separator1 + SEPARATOR.length)
        val nested2 = u2.file.substring(separator2 + SEPARATOR.length)
        if (nested1 != nested2) {
            val canonical1 = canonicalize(nested1)
            val canonical2 = canonicalize(nested2)
            if (canonical1 != canonical2) {
                return false
            }
        }
        val root1 = u1.file.substring(0, separator1)
        val root2 = u2.file.substring(0, separator2)
        try {
            return super.sameFile(URL(root1), URL(root2))
        } catch (ex: MalformedURLException) {
            // Continue
        }
        return super.sameFile(u1, u2)
    }

    private fun canonicalize(path: String): String = SEPARATOR_PATTERN.matcher(path).replaceAll("/")

    /**
     * 根据一个给定的URL, 去获取该URL的RootJarFile
     *
     * @param url url
     * @return 该URL对应的RootJarFile
     */
    @Throws(IOException::class)
    fun getRootJarFileFromUrl(url: URL): JarFile {
        val spec = url.file
        // 找到URL当中的"!/"
        val separatorIndex = spec.indexOf(SEPARATOR)
        if (separatorIndex == -1) {
            throw MalformedURLException("Jar URL does not contain !/ separator")
        }
        // 把URL当中的"!/"后面的部分去掉
        val name = spec.substring(0, separatorIndex)
        return getRootJarFile(name)
    }

    /**
     * 根据urlString(fileName)去获取到该URL的RootJar
     *
     * @param name urlString(fileName)
     * @return RootJarFile
     */
    @Throws(IOException::class)
    private fun getRootJarFile(name: String): JarFile {
        return try {
            check(name.startsWith(FILE_PROTOCOL)) { "给定的url不是一个文件协议(file:)的URL" }
            val file = File(URI.create(name))
            val cache = rootFileCache.get()
            var result = cache?.get(file)
            if (result == null) {
                result = JarFile(file)
                addToRootFileCache(file, result)
            }
            result
        } catch (ex: Exception) {
            throw IOException("Unable to open root Jar file '$name'", ex)
        }
    }
}