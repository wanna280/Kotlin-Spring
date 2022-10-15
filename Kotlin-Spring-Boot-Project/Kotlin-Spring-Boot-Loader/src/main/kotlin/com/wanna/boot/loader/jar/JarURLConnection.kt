package com.wanna.boot.loader.jar

import com.wanna.boot.loader.jar.AsciiBytes.Companion.toString
import java.io.*
import java.net.*
import java.security.Permission
import java.util.jar.JarEntry

/**
 * 为[JarFile.getUrl]方法返回的URL去提供支持的[java.net.JarURLConnection]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/5
 * @see Handler
 * @see JarFile
 */
internal class JarURLConnection constructor(
    url: URL,
    private val jarFile: AbstractJarFile?,
    private val jarEntryName: JarEntryName?,
) : java.net.JarURLConnection(EMPTY_JAR_URL) {
    companion object {
        private val useFastExceptions = ThreadLocal<Boolean>()
        private val FILE_NOT_FOUND_EXCEPTION = FileNotFoundException("Jar file or entry not found")
        private val NOT_FOUND_CONNECTION_EXCEPTION = IllegalStateException(FILE_NOT_FOUND_EXCEPTION)

        private const val SEPARATOR = "!/"

        @JvmField
        var EMPTY_JAR_URL = URL("jar:", null, 0, "file:!/", object : URLStreamHandler() {
            @Throws(IOException::class)
            override fun openConnection(u: URL): URLConnection? {
                // Stub URLStreamHandler to prevent the wrong JAR Handler from being
                // Instantiated and cached.
                return null
            }
        })

        private val EMPTY_JAR_ENTRY_NAME = JarEntryName(StringSequence(""))
        private val NOT_FOUND_CONNECTION = notFound()


        @JvmStatic
        fun setUseFastExceptions(useFastExceptions: Boolean) = this.useFastExceptions.set(useFastExceptions)

        @JvmStatic
        @Throws(IOException::class)
        operator fun get(url: URL, jarFile: JarFile): JarURLConnection {
            var jarFile = jarFile
            val spec = StringSequence(url.file)
            var index = indexOfRootSpec(spec, jarFile.pathFromRoot)
            if (index == -1) {
                return if (java.lang.Boolean.TRUE == useFastExceptions.get()) NOT_FOUND_CONNECTION
                else JarURLConnection(url, null, EMPTY_JAR_ENTRY_NAME)
            }
            var separator: Int
            while (spec.indexOf(SEPARATOR, index).also { separator = it } > 0) {
                val entryName: JarEntryName = JarEntryName[spec.subSequence(index, separator)]
                val jarEntry = jarFile.getJarEntry(entryName.toCharSequence())
                    ?: return notFound(jarFile, entryName)
                jarFile = jarFile.getNestedJarFile(jarEntry)
                index = separator + SEPARATOR.length
            }
            val jarEntryName = JarEntryName[spec, index]
            return if (java.lang.Boolean.TRUE == useFastExceptions.get() && !jarEntryName.isEmpty
                && !jarFile.containsEntry(jarEntryName.toString())
            ) {
                NOT_FOUND_CONNECTION
            } else JarURLConnection(url, jarFile.wrapper!!, jarEntryName)
        }

        private fun indexOfRootSpec(file: StringSequence, pathFromRoot: String): Int {
            val separatorIndex = file.indexOf(SEPARATOR)
            return if (separatorIndex < 0 || !file.startsWith(pathFromRoot, separatorIndex)) {
                -1
            } else separatorIndex + SEPARATOR.length + pathFromRoot.length
        }

        private fun notFound(): JarURLConnection {
            return try {
                notFound(null, null)
            } catch (ex: IOException) {
                throw IllegalStateException(ex)
            }
        }

        @Throws(IOException::class)
        private fun notFound(jarFile: JarFile?, jarEntryName: JarEntryName?): JarURLConnection {
            return if (java.lang.Boolean.TRUE == useFastExceptions.get()) NOT_FOUND_CONNECTION
            else JarURLConnection(EMPTY_JAR_URL, jarFile, jarEntryName)
        }
    }

    init {
        this.url = url
    }

    private var permission: Permission? = null
    private var jarFileUrl: URL? = null
    private var jarEntry: JarEntry? = null

    @Throws(IOException::class)
    override fun connect() {
        jarFile ?: throw NOT_FOUND_CONNECTION_EXCEPTION
        if (!jarEntryName!!.isEmpty && jarEntry == null) {
            jarEntry = jarFile.getJarEntry(entryName)
            jarEntry ?: throwFileNotFound(jarEntryName, jarFile)
        }
        connected = true
    }

    @Throws(IOException::class)
    override fun getJarFile(): java.util.jar.JarFile {
        connect()
        return jarFile!!
    }

    override fun getJarFileURL(): URL {
        jarFile ?: throw NOT_FOUND_CONNECTION_EXCEPTION
        this.jarFileUrl = jarFileUrl ?: buildJarFileUrl()
        return jarFileUrl!!
    }

    private fun buildJarFileUrl(): URL {
        return try {
            var spec = jarFile!!.getUrl().file
            if (spec.endsWith(SEPARATOR)) {
                spec = spec.substring(0, spec.length - SEPARATOR.length)
            }
            if (!spec.contains(SEPARATOR)) {
                URL(spec)
            } else URL("jar:$spec")
        } catch (ex: MalformedURLException) {
            throw IllegalStateException(ex)
        }
    }

    @Throws(IOException::class)
    override fun getJarEntry(): JarEntry? {
        if (jarEntryName == null || jarEntryName.isEmpty) {
            return null
        }
        connect()
        return jarEntry!!
    }

    override fun getEntryName(): String {
        jarFile ?: throw NOT_FOUND_CONNECTION_EXCEPTION
        return jarEntryName.toString()
    }

    @Throws(IOException::class)
    override fun getInputStream(): InputStream {
        jarFile ?: throw NOT_FOUND_CONNECTION_EXCEPTION
        if (jarEntryName!!.isEmpty && jarFile.getType() === AbstractJarFile.JarFileType.DIRECT) {
            throw IOException("no entry name specified")
        }
        connect()
        val inputStream = if (jarEntryName.isEmpty) jarFile.getInputStream() else jarFile.getInputStream(jarEntry!!)
        if (inputStream == null) {
            throwFileNotFound(jarEntryName, jarFile)
        }
        return inputStream!!
    }

    @Throws(FileNotFoundException::class)
    private fun throwFileNotFound(entry: Any?, jarFile: AbstractJarFile) {
        if (java.lang.Boolean.TRUE == useFastExceptions.get()) {
            throw FILE_NOT_FOUND_EXCEPTION
        }
        throw FileNotFoundException("JAR entry " + entry + " not found in " + jarFile.name)
    }

    override fun getContentLength(): Int {
        val length = contentLengthLong
        return if (length > Int.MAX_VALUE) {
            -1
        } else length.toInt()
    }

    override fun getContentLengthLong(): Long {
        return if (jarFile == null) {
            -1
        } else try {
            if (jarEntryName!!.isEmpty) {
                return jarFile.size().toLong()
            }
            val entry = getJarEntry()
            entry?.size?.toInt()?.toLong() ?: -1
        } catch (ex: IOException) {
            -1
        }
    }

    @Throws(IOException::class)
    override fun getContent(): Any? {
        connect()
        return if (jarEntryName!!.isEmpty) jarFile else super.getContent()
    }

    override fun getContentType(): String? {
        return if (jarEntryName != null) jarEntryName.contentType!! else null
    }

    @Throws(IOException::class)
    override fun getPermission(): Permission {
        jarFile ?: throw FILE_NOT_FOUND_EXCEPTION
        this.permission = permission ?: jarFile.getPermission()
        return permission!!
    }

    override fun getLastModified(): Long {
        return if (jarFile == null || jarEntryName!!.isEmpty) {
            0
        } else try {
            val entry = getJarEntry()
            entry?.time ?: 0
        } catch (ex: IOException) {
            0
        }
    }

    /**
     * A JarEntryName parsed from a URL String.
     */
    internal class JarEntryName(spec: StringSequence) {
        private val name: StringSequence = decode(spec)
        var contentType: String? = null
            get() {
                if (field == null) {
                    field = deduceContentType()
                }
                return field
            }
            private set

        private fun decode(source: StringSequence): StringSequence {
            if (source.isEmpty || source.indexOf('%') < 0) {
                return source
            }
            val bos = ByteArrayOutputStream(source.length)
            write(source.toString(), bos)
            // AsciiBytes is what is used to store the JarEntries so make it symmetric
            return StringSequence(toString(bos.toByteArray()))
        }

        private fun write(source: String, outputStream: ByteArrayOutputStream) {
            val length = source.length
            var i = 0
            while (i < length) {
                var c = source[i].code
                if (c > 127) {
                    try {
                        val encoded = URLEncoder.encode(c.toChar().toString(), "UTF-8")
                        write(encoded, outputStream)
                    } catch (ex: UnsupportedEncodingException) {
                        throw IllegalStateException(ex)
                    }
                } else {
                    if (c == '%'.code) {
                        require(i + 2 < length) { "Invalid encoded sequence \"" + source.substring(i) + "\"" }
                        c = decodeEscapeSequence(source, i).code
                        i += 2
                    }
                    outputStream.write(c)
                }
                i++
            }
        }

        private fun decodeEscapeSequence(source: String, i: Int): Char {
            val hi = source[i + 1].digitToIntOrNull(16) ?: -1
            val lo = source[i + 2].digitToIntOrNull(16) ?: -1
            require(!(hi == -1 || lo == -1)) { "Invalid encoded sequence \"" + source.substring(i) + "\"" }
            return ((hi shl 4) + lo).toChar()
        }

        fun toCharSequence(): CharSequence = this.name

        override fun toString() = this.name.toString()

        val isEmpty: Boolean
            get() = name.isEmpty

        private fun deduceContentType(): String {
            // Guess the content type, don't bother with streams as mark is not supported
            var type = if (isEmpty) "x-java/jar" else null
            type = (type ?: guessContentTypeFromName(toString())) ?: "content/unknown"
            return type
        }

        companion object {
            @JvmOverloads
            operator fun get(spec: StringSequence, beginIndex: Int = 0): JarEntryName =
                if (spec.length <= beginIndex) EMPTY_JAR_ENTRY_NAME else JarEntryName(spec.subSequence(beginIndex))
        }
    }
}