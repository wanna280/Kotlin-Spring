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

        /**
         * 文件的读操作权限的字符串常量
         */
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

    /**
     * 根JarFile
     */
    val rootJarFile: RandomAccessDataFile

    /**
     * 以根JarFile作为基准，当前JarFile的相对路径
     */
    val pathFromRoot: String

    /**
     * 得到的当前Jar包当中的真正的归档文件(去掉了prefix等部分的数据)
     *
     * @see RandomAccessData
     * @see CentralDirectoryParser.parse
     */
    var data: RandomAccessData

    /**
     * Jar包的文件类型
     *
     * * DIRECT-直接Jar包
     * * NESTED_DIRECTORY-嵌套的文件夹
     * * NESTED_JAR-嵌套的Jar包
     */
    private val type: JarFileType

    /**
     * 当前JarFile所在的URL
     *
     * @see getUrl
     */
    private var url: URL? = null

    /**
     * 当前JarFile的URL字符串
     */
    @get:Throws(MalformedURLException::class)
    var urlString: String? = null
        get() {
            if (field == null) {
                field = getUrl().toString()
            }
            return field
        }
        private set

    /**
     * 当前JarFile当中的JarEntry列表
     */
    private val entries: JarFileEntries

    /**
     * 用于获取Manifest的Supplier
     */
    private val manifestSupplier: Supplier<Manifest>

    /**
     * Manifest
     */
    private var manifest: SoftReference<Manifest>? = null

    /**
     * 当前JarFile是否已经被签名？
     */
    var isSigned = false
        private set

    /**
     * 当前JarFile的评论信息
     */
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

        // 跟Jar包
        rootJarFile = rootFile

        // 当前JarFile相对于根的相对路径
        this.pathFromRoot = pathFromRoot

        // 创建一个CentralDirectory的解析器
        val parser = CentralDirectoryParser()

        // 将JarFileEntries作为Visitor添加到Parser当中
        // 在执行visitStart/FileHeader/visitEnd回调方法时，可以自动将相关的信息去设置到JarFileEntries当中
        entries = parser.addVisitor(JarFileEntries(this, filter))

        // JarType(DIRECT/NESTED_DIRECTORY,/NESTED_JAR)
        this.type = type

        // 添加一个CentralDirectoryVisitor，用于将comment和isSigned去统计出来保存到当前的JarFile对象当中
        parser.addVisitor(centralDirectoryVisitor())
        try {
            // 执行真正的CentralDirectory的解析工作，解析得到真正的Archive归档数据(跳过prefixBytes)...
            // 并回调Visitor的visitStart/visitFileHeader/visitEnd回调方法完成相关数据的统计工作...
            this.data = parser.parse(data, filter == null)!!
        } catch (ex: RuntimeException) {
            try {
                rootJarFile.close()
                super.close()
            } catch (ioe: IOException) {
            }
            throw ex
        }

        // 用于去解析JarFile的Manifest的Supplier，也就是去读取当前Jar包下的"META-INF/MANIFEST.MF"文件的输入流
        this.manifestSupplier = manifestSupplier
            ?: Supplier {
                try {
                    getInputStream(MANIFEST_NAME).use { inputStream -> Manifest(inputStream) }
                } catch (ex: IOException) {
                    throw RuntimeException(ex)
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
        data: RandomAccessData?,
        type: JarFileType
    ) : this(rootFile, pathFromRoot, data, null, type, null)

    /**
     * 创建一个访问CentralDirectory的Visitor，我们需要它的回调，去保存一些信息(comment和isSigned)
     */
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

    /**
     * 获取当前JarFile的权限
     *
     * @return 只读权限
     */
    override fun getPermission() = FilePermission(rootJarFile.file.path, READ_ACTION)

    /**
     * 获取当前JarFile的Manifest，通过读取"META-INF/MANIFEST.SF"文件获取
     *
     * @return Manifest
     */
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

    /**
     * 获取到用于迭代JarEntry的stream流
     *
     * @return 迭代JarEntry的stream
     */
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
    override fun iterator(): Iterator<java.util.jar.JarEntry> = entries.iterator { ensureOpen() }

    /**
     * 根据文件名，从Jar包内部去获取到JarEntry
     *
     * @param name 文件名
     * @return JarEntry
     */
    fun getJarEntry(name: CharSequence): JarEntry? = entries.getEntry(name)

    /**
     * 根据文件名，从Jar包内部去获取到JarEntry
     *
     * @param name 文件名
     * @return JarEntry
     */
    override fun getJarEntry(name: String): JarEntry? = getEntry(name) as JarEntry?

    /**
     * 根据文件名，判断Jar包当中是否存在有该JarEntry？
     *
     * @param name 文件名
     * @return 如果存在，return true；不存在return false
     */
    fun containsEntry(name: String): Boolean = entries.containsEntry(name)

    /**
     * 根据文件名，从Jar包内部去获取到JarEntry
     *
     * @param name 文件名
     * @return JarEntry
     */
    override fun getEntry(name: String): ZipEntry? {
        ensureOpen()
        return entries.getEntry(name)
    }

    /**
     * 获取当前JarFile的输入流
     *
     * @return 输入流
     */
    @Throws(IOException::class)
    override fun getInputStream() = data.getInputStream()

    /**
     * 获取指定的ZipEntry的输入流
     *
     * @return 输入流
     */
    @Synchronized
    @Throws(IOException::class)
    override fun getInputStream(entry: ZipEntry): InputStream {
        ensureOpen()
        return if (entry is JarEntry) entries.getInputStream(entry)!! else getInputStream(entry.name)!!
    }

    /**
     * 根据entryName去获取到该ArchiveEntry的输入流
     *
     * @param name entryName
     * @return InputStream
     */
    @Throws(IOException::class)
    fun getInputStream(name: String): InputStream? = entries.getInputStream(name)


    /**
     * 根据一个JarEntry去将它转换成为一个JarFile
     *
     * @return JarFile
     */
    @Synchronized
    @Throws(IOException::class)
    fun getNestedJarFile(entry: ZipEntry): JarFile = getNestedJarFile(entry as JarEntry)


    /**
     * 根据给定的JarEntry去生成一个嵌套的JarFile
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
     * 根据JarEntry去创建JarFile，支持是一个文件夹(比如"BOOT-INF/classes/")，也支持是一个文件，比如"xxx.jar"
     *
     * @param entry JarEntry
     * @return JarFile
     */
    @Throws(IOException::class)
    private fun createJarFileFromEntry(entry: JarEntry): JarFile {
        return if (entry.isDirectory) createJarFileFromDirectoryEntry(entry)
        else createJarFileFromFileEntry(entry)
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
        // 如果需要对内部的文件去进行创建JarFile(Jar包)，那么内部的JarFile(Jar包)的存放方式只能是直接存放，不能被压缩过...
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

    /**
     * 获取当前JarFile的Comment
     *
     * @return comment
     */
    override fun getComment(): String? {
        ensureOpen()
        return comment
    }

    /**
     * 获取当前JarFile当中的的JarEntry的数量
     *
     * @return size of JarEntry
     */
    override fun size(): Int {
        ensureOpen()
        return entries.size
    }

    /**
     * 关闭当前JarFile的输入流
     */
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

    /**
     * 获取当前JarFile的URL，如果之前没有的话，那么我们需要先生成再去进行保存；
     * 这个方法返回的URL，将会被用于类加载器的类加载工作
     *
     * @return JarFile URL
     * @see com.wanna.boot.loader.LaunchedURLClassLoader
     */
    @Throws(MalformedURLException::class)
    override fun getUrl(): URL {
        if (url == null) {
            // 生成自定义的URL，在Jar的路径之后去添加一个"!/"路径，符合原生Java当中的Jar包URL的规范
            var file = rootJarFile.file.toURI().toString() + pathFromRoot + "!/"
            file = file.replace("file:////", "file://")

            // 构建一个URL，并且指定解析该URL的URLStreamHandler为我们自定义的Handler
            // 因为正常的URLStreamHandler无法去解析我们构建出来的这样格式的URL
            url = URL("jar", "", -1, file, Handler(this))
        }
        return url!!
    }

    override fun toString() = this.name

    override fun getName(): String = rootJarFile.file.toString() + pathFromRoot

    fun getCertification(entry: JarEntry): JarEntryCertification {
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


    /**
     * 获取当前JarFile的Jar包的文件类型
     *
     * * DIRECT-直接Jar包
     * * NESTED_DIRECTORY-嵌套的文件夹
     * * NESTED_JAR-嵌套的Jar包
     *
     * @return JarFileType
     */
    override fun getType() = this.type

    /**
     * 获取当前JarFile的Jar包的文件类型
     *
     * * DIRECT-直接Jar包
     * * NESTED_DIRECTORY-嵌套的文件夹
     * * NESTED_JAR-嵌套的Jar包
     *
     * @return JarFileType
     */
    override fun getJarFileType() = getType()

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