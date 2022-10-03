package com.wanna.boot.loader.jar


import com.wanna.boot.loader.data.RandomAccessData
import com.wanna.boot.loader.data.RandomAccessDataFile
import java.io.File
import java.io.FilePermission
import java.io.IOException
import java.io.InputStream
import java.lang.ref.SoftReference
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import java.util.function.Supplier
import java.util.jar.Manifest
import java.util.stream.Stream
import java.util.stream.StreamSupport
import java.util.zip.ZipEntry


/**
 * 在[java.util.jar.JarFile]的基础上去扩展相关的功能的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/4
 */
class JarFile private constructor(
    rootFile: RandomAccessDataFile, pathFromRoot: String, data: RandomAccessData?, filter: JarEntryFilter?,
    type: JarFileType, manifestSupplier: Supplier<Manifest>?
) : AbstractJarFile(rootFile.file), Iterable<java.util.jar.JarEntry> {

    companion object {
        /**
         * Manifest文件名
         */
        private const val MANIFEST_NAME = "META-INF/MANIFEST.MF"
        private const val PROTOCOL_HANDLER = "java.protocol.handler.pkgs"
        private const val HANDLERS_PACKAGE = "com.wanna.boot.loader"
        private val META_INF = AsciiBytes("META-INF/")
        private val SIGNATURE_FILE_EXTENSION = AsciiBytes(".SF")
        private const val READ_ACTION = "read"

        /**
         * 注册一个"java.protocol.handler.pkgs"属性到SystemProperties当中，
         * 以便[java.net.URLStreamHandler]可以定位到，用来处理那些Jar的URL
         */
        @JvmStatic
        fun registerUrlProtocolHandler() {
            Handler.captureJarContextUrl()
            val handlers = System.getProperty(PROTOCOL_HANDLER, "")
            System.setProperty(
                PROTOCOL_HANDLER,
                if (handlers == null || handlers.isEmpty()) HANDLERS_PACKAGE else "$handlers|$HANDLERS_PACKAGE"
            )
            resetCachedUrlHandlers()
        }

        /**
         * 重设已经缓存的URLStreamHandler
         */
        @JvmStatic
        private fun resetCachedUrlHandlers() {
            try {
                URL.setURLStreamHandlerFactory(null)
            } catch (ex: Error) {
                // Ignore
            }
        }
    }

    protected val rootJarFile: RandomAccessDataFile
    val pathFromRoot: String
    var data: RandomAccessData? = null
    private val type: JarFileType
    private var url: URL? = null

    @get:Throws(MalformedURLException::class)
    var urlString: String? = null
        get() {
            if (field == null) {
                field = getUrl().toString()
            }
            return field
        }
        private set
    private val entries: JarFileEntries
    private val manifestSupplier: Supplier<Manifest>
    private var manifest: SoftReference<Manifest?>? = null
    var isSigned = false
        private set
    private var comment: String? = null

    @Volatile
    var isClosed = false
        private set

    @get:Throws(IOException::class)
    @Volatile
    var wrapper: JarFileWrapper? = null
        get() {
            var wrapper = field
            if (wrapper == null) {
                wrapper = JarFileWrapper(this)
                field = wrapper
            }
            return wrapper
        }
        private set

    init {
        super.close()
        rootJarFile = rootFile
        this.pathFromRoot = pathFromRoot
        val parser = CentralDirectoryParser()
        entries = parser.addVisitor(JarFileEntries(this, filter))
        this.type = type
        parser.addVisitor(centralDirectoryVisitor())
        try {
            this.data = parser.parse(data, filter == null)
        } catch (ex: RuntimeException) {
            try {
                rootJarFile.close()
                super.close()
            } catch (ioex: IOException) {
            }
            throw ex
        }
        this.manifestSupplier = manifestSupplier
            ?: Supplier {
                try {
                    getInputStream(MANIFEST_NAME).use { inputStream ->
                        Manifest(inputStream)
                    }
                } catch (ex: IOException) {
                    throw RuntimeException(ex)
                } finally {
                }
            }
    }


    /**
     * 根据给定的File去创建JarFile
     *
     * @param file File
     */
    constructor(file: File) : this(RandomAccessDataFile(file))

    /**
     * 根据给定的File去创建JarFile
     *
     * @param file File
     */
    internal constructor(file: RandomAccessDataFile) : this(file, "", file, JarFileType.DIRECT)

    /**
     * 为直接创建JarFile和从嵌套ArchiveEntry去提供私有构造器
     */
    private constructor(
        rootFile: RandomAccessDataFile,
        pathFromRoot: String,
        data: RandomAccessData,
        type: JarFileType
    ) : this(rootFile, pathFromRoot, data, null, type, null)

    private fun centralDirectoryVisitor(): CentralDirectoryVisitor {
        return object : CentralDirectoryVisitor {
            override fun visitStart(endRecord: CentralDirectoryEndRecord, centralDirectoryData: RandomAccessData) {
                comment = endRecord.comment
            }

            override fun visitFileHeader(fileHeader: CentralDirectoryFileHeader, dataOffset: Long) {
                val name = fileHeader.getName()
                if (name!!.startsWith(META_INF) && name.endsWith(SIGNATURE_FILE_EXTENSION)) {
                    isSigned = true
                }
            }

            override fun visitEnd() {}
        }
    }

    override fun getPermission() = FilePermission(rootJarFile.file.path, READ_ACTION)

    @Throws(IOException::class)
    override fun getManifest(): Manifest? {
        var manifest = if (manifest != null) manifest!!.get() else null
        if (manifest == null) {
            manifest = try {
                manifestSupplier.get()
            } catch (ex: RuntimeException) {
                throw IOException(ex)
            }
            this.manifest = SoftReference(manifest)
        }
        return manifest
    }

    /**
     * 获取用于JarEntry的迭代的[Enumeration]
     *
     * @return 迭代JarEntry的[Enumeration]
     */
    override fun entries(): Enumeration<java.util.jar.JarEntry> = JarEntryEnumeration(entries.iterator())

    override fun stream(): Stream<java.util.jar.JarEntry> {
        val spliterator = Spliterators.spliterator(
            iterator(), size().toLong(),
            Spliterator.ORDERED or Spliterator.DISTINCT or Spliterator.IMMUTABLE or Spliterator.NONNULL
        )
        return StreamSupport.stream(spliterator, false)
    }

    /**
     * 返回JarFile内部的JarEntry列表的迭代器
     *
     * @return JarFileEntry迭代器
     */
    override fun iterator(): MutableIterator<java.util.jar.JarEntry> = entries.iterator { ensureOpen() }

    fun getJarEntry(name: CharSequence): JarEntry = entries.getEntry(name)

    override fun getJarEntry(name: String): JarEntry = getEntry(name) as JarEntry

    fun containsEntry(name: String): Boolean = entries.containsEntry(name)

    override fun getEntry(name: String): ZipEntry {
        ensureOpen()
        return entries.getEntry(name)
    }

    override fun getJarFileType() = getType()

    @Throws(IOException::class)
    override fun getInputStream() = data!!.getInputStream()

    @Synchronized
    @Throws(IOException::class)
    override fun getInputStream(entry: ZipEntry): InputStream {
        ensureOpen()
        return if (entry is JarEntry) {
            entries.getInputStream(entry)
        } else getInputStream(entry.name)
    }

    /**
     * 根据entryName去获取到该ArchiveEntry的输入流
     *
     * @param name entryName
     * @return InputStream
     */
    @Throws(IOException::class)
    fun getInputStream(name: String): InputStream = entries.getInputStream(name)

    /**
     * Return a nested [JarFile] loaded from the specified entry.
     *
     * @param entry the zip entry
     * @return a [JarFile] for the entry
     * @throws IOException if the nested jar file cannot be read
     */
    @Synchronized
    @Throws(IOException::class)
    fun getNestedJarFile(entry: ZipEntry): JarFile = getNestedJarFile(entry as JarEntry)


    /**
     * 根据给定的JarEntry去生成一个嵌套的JarFiel
     *
     * @param entry JarEntry
     * @return JarFile
     */
    @Synchronized
    @Throws(IOException::class)
    fun getNestedJarFile(entry: JarEntry): JarFile {
        return try {
            createJarFileFromEntry(entry)
        } catch (ex: Exception) {
            throw IOException("Unable to open nested jar file '" + entry.name + "'", ex)
        }
    }

    /**
     * 根据JarEntry去创建JarFile
     *
     * @param entry JarEntry
     * @return JarFile
     */
    @Throws(IOException::class)
    private fun createJarFileFromEntry(entry: JarEntry): JarFile {
        return if (entry.isDirectory) {
            createJarFileFromDirectoryEntry(entry)
        } else createJarFileFromFileEntry(entry)
    }

    /**
     * 从一个目录的JarEntry去创建JarFile
     *
     * @param entry JarEntry
     * @return JarFile
     */
    @Throws(IOException::class)
    private fun createJarFileFromDirectoryEntry(entry: JarEntry): JarFile {
        val entryName = entry.asciiBytesName
        val filter: JarEntryFilter = object : JarEntryFilter {
            override fun apply(name: AsciiBytes) =
                if (name.startsWith(entryName) && name != entryName) name.substring(entryName.length()) else null
        }
        return JarFile(
            rootJarFile, pathFromRoot + "!/" + entry.name.substring(0, entryName.length() - 1),
            data, filter, JarFileType.NESTED_DIRECTORY, manifestSupplier
        )
    }

    /**
     * 从一个FileEntry去创建JarFile
     *
     * @param entry JarEntry
     * @return JarFile
     */
    @Throws(IOException::class)
    private fun createJarFileFromFileEntry(entry: JarEntry): JarFile {
        check(entry.method == ZipEntry.STORED) {
            ("Unable to open nested entry '" + entry.name + "'. It has been compressed and nested "
                    + "jar files must be stored without compression. Please check the "
                    + "mechanism used to create your executable jar file")
        }
        val entryData = entries.getEntryData(entry.name)
        return JarFile(
            rootJarFile, pathFromRoot + "!/" + entry.name, entryData,
            JarFileType.NESTED_JAR
        )
    }

    override fun getComment(): String {
        ensureOpen()
        return comment!!
    }

    override fun size(): Int {
        ensureOpen()
        return entries.size
    }

    @Throws(IOException::class)
    override fun close() {
        if (isClosed) {
            return
        }
        super.close()
        if (type === JarFileType.DIRECT) {
            rootJarFile.close()
        }
        isClosed = true
    }

    private fun ensureOpen() {
        check(!isClosed) { "ZIP文件已经被关闭" }
    }

    @Throws(MalformedURLException::class)
    override fun getUrl(): URL {
        if (url == null) {
            var file = rootJarFile.file.toURI().toString() + pathFromRoot + "!/"
            file = file.replace("file:////", "file://") // Fix UNC paths
            url = URL("jar", "", -1, file, Handler(this))
        }
        return url!!
    }

    override fun toString() = this.name

    override fun getName(): String = rootJarFile.file.toString() + pathFromRoot

    fun getCertification(entry: JarEntry?): JarEntryCertification {
        return try {
            entries.getCertification(entry)
        } catch (ex: IOException) {
            throw IllegalStateException(ex)
        }
    }

    /**
     * 清除JarEntries的缓存
     */
    fun clearCache() = entries.clearCache()

    override fun getType() = this.type

    /**
     * 提供[java.util.jar.JarEntry]的迭代的[Enumeration]
     *
     * @param iterator 迭代JarEntry的迭代器
     */
    private class JarEntryEnumeration(private val iterator: Iterator<JarEntry>) : Enumeration<java.util.jar.JarEntry> {
        override fun hasMoreElements() = iterator.hasNext()
        override fun nextElement() = iterator.next()
    }
}