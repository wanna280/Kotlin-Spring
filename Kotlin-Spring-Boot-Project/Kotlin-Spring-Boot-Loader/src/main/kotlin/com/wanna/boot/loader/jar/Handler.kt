package com.wanna.boot.loader.jar

import java.io.File
import java.io.IOException
import java.lang.ref.SoftReference
import java.net.*
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level
import java.util.logging.Logger
import java.util.regex.Pattern
import kotlin.collections.set

/**
 * 为SpringBoot的Loader提供Jar包的读取的[URLStreamHandler]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/5
 */
class Handler @JvmOverloads constructor(private val jarFile: JarFile? = null) : URLStreamHandler() {
    companion object {
        private const val JAR_PROTOCOL = "jar:"
        private const val FILE_PROTOCOL = "file:"
        private const val TOMCAT_WARFILE_PROTOCOL = "war:file:"
        private const val SEPARATOR = "!/"
        private val SEPARATOR_PATTERN = Pattern.compile(SEPARATOR, Pattern.LITERAL)
        private const val CURRENT_DIR = "/./"
        private val CURRENT_DIR_PATTERN = Pattern.compile(CURRENT_DIR, Pattern.LITERAL)
        private const val PARENT_DIR = "/../"
        private const val PROTOCOL_HANDLER = "java.protocol.handler.pkgs"
        private val FALLBACK_HANDLERS = arrayOf("sun.net.www.protocol.jar.Handler")
        private var jarContextUrl: URL? = null
        private var rootFileCache: SoftReference<MutableMap<File, JarFile>> = SoftReference(null)

        @JvmStatic
        fun setUseFastConnectionExceptions(useFastConnectionExceptions: Boolean) =
            JarURLConnection.setUseFastExceptions(useFastConnectionExceptions)

        /**
         * Add the given [JarFile] to the root file cache.
         *
         * @param sourceFile the source file to add
         * @param jarFile    the jar file.
         */
        fun addToRootFileCache(sourceFile: File, jarFile: JarFile) {
            var cache = rootFileCache.get()
            if (cache == null) {
                cache = ConcurrentHashMap()
                rootFileCache = SoftReference(cache)
            }
            cache[sourceFile] = jarFile
        }

        /**
         * 如果可能的话，捕捉一个可以被原来的JarHandler去进行配置的URL；以便后续我们可以使用它去作为Fallback的上下文；
         * 我们仅仅只是想要知道一下最原始是什么，很快我们就会将JarHandler给重设回去
         */
        fun captureJarContextUrl() {
            if (canResetCachedUrlHandlers()) {
                val handlers = System.getProperty(PROTOCOL_HANDLER)
                try {
                    System.clearProperty(PROTOCOL_HANDLER)
                    try {
                        resetCachedUrlHandlers()
                        jarContextUrl = URL("jar:file:context.jar!/")
                        val connection = jarContextUrl!!.openConnection()
                        if (connection is JarURLConnection) {
                            jarContextUrl = null
                        }
                    } catch (ex: Exception) {
                    }
                } finally {
                    if (handlers == null) {
                        System.clearProperty(PROTOCOL_HANDLER)
                    } else {
                        System.setProperty(PROTOCOL_HANDLER, handlers)
                    }
                }
                resetCachedUrlHandlers()
            }
        }

        @JvmStatic
        private fun canResetCachedUrlHandlers(): Boolean {
            return try {
                resetCachedUrlHandlers()
                true
            } catch (ex: Error) {
                false
            }
        }

        @JvmStatic
        private fun resetCachedUrlHandlers() = URL.setURLStreamHandlerFactory(null)
    }

    /**
     * 检查所有的候选的URLStreamHandler，去获取Fallback的URLStreamHandler
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

    @Throws(IOException::class)
    override fun openConnection(url: URL): URLConnection {
        return if (jarFile != null && isUrlInJarFile(url, jarFile)) {
            JarURLConnection[url, jarFile]
        } else try {
            JarURLConnection[url, getRootJarFileFromUrl(url)]
        } catch (ex: Exception) {
            openFallbackConnection(url, ex)
        }
    }

    @Throws(MalformedURLException::class)
    private fun isUrlInJarFile(url: URL, jarFile: JarFile): Boolean {
        // Try the path first to save building a new url string each time
        return (url.path.startsWith(jarFile.getUrl().path)
                && url.toString().startsWith(jarFile.urlString!!))
    }

    @Throws(IOException::class)
    private fun openFallbackConnection(url: URL, reason: Exception): URLConnection {
        return try {
            var connection = openFallbackTomcatConnection(url)
            connection = connection ?: openFallbackContextConnection(url)
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
     * Attempt to open a Tomcat formatted 'jar:war:file:...' URL. This method allows us to
     * use our own nested JAR support to open the content rather than the logic in
     * `sun.net.www.protocol.jar.URLJarFile` which will extract the nested jar to
     * the temp folder to that its content can be accessed.
     *
     * @param url the URL to open
     * @return a [URLConnection] or `null`
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
            }
        }
        return null
    }

    private fun isTomcatWarUrl(file: String): Boolean {
        if (file.startsWith(TOMCAT_WARFILE_PROTOCOL) || !file.contains("*/")) {
            try {
                val connection = URL(file).openConnection()
                if (connection.javaClass.name.startsWith("org.apache.catalina")) {
                    return true
                }
            } catch (ex: Exception) {
            }
        }
        return false
    }

    /**
     * Attempt to open a fallback connection by using a context URL captured before the
     * jar handler was replaced with our own version. Since this method doesn't use
     * reflection it won't trigger "illegal reflective access operation has occurred"
     * warnings on Java 13+.
     *
     * @param url the URL to open
     * @return a [URLConnection] or `null`
     */
    private fun openFallbackContextConnection(url: URL): URLConnection? {
        try {
            if (jarContextUrl != null) {
                return URL(jarContextUrl, url.toExternalForm()).openConnection()
            }
        } catch (ex: Exception) {
        }
        return null
    }

    /**
     * Attempt to open a fallback connection by using reflection to access Java's default
     * jar [URLStreamHandler].
     *
     * @param url the URL to open
     * @return the [URLConnection]
     * @throws Exception if not connection could be opened
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

    private fun replaceCurrentDir(file: String): String {
        return CURRENT_DIR_PATTERN.matcher(file).replaceAll("/")
    }

    override fun hashCode(u: URL): Int {
        return hashCode(u.protocol, u.file)
    }

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

    private fun canonicalize(path: String): String {
        return SEPARATOR_PATTERN.matcher(path).replaceAll("/")
    }

    @Throws(IOException::class)
    fun getRootJarFileFromUrl(url: URL): JarFile {
        val spec = url.file
        val separatorIndex = spec.indexOf(SEPARATOR)
        if (separatorIndex == -1) {
            throw MalformedURLException("Jar URL does not contain !/ separator")
        }
        val name = spec.substring(0, separatorIndex)
        return getRootJarFile(name)
    }

    @Throws(IOException::class)
    private fun getRootJarFile(name: String): JarFile {
        return try {
            check(name.startsWith(FILE_PROTOCOL)) { "Not a file URL" }
            val file = File(URI.create(name))
            val cache: Map<File, JarFile>? = rootFileCache!!.get()
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