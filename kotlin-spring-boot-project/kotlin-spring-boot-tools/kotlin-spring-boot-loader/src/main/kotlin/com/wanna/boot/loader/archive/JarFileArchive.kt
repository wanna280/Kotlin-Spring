package com.wanna.boot.loader.archive

import com.wanna.boot.loader.jar.JarFile
import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.file.*
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermission.*
import java.nio.file.attribute.PosixFilePermissions
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.Manifest
import javax.annotation.Nullable

/**
 * Jar包格式的Java归档文件, 通过内部组合一个JarFile去完成功能的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/26
 * @param jarFile JarFile
 */
open class JarFileArchive(private val jarFile: JarFile) : Archive {

    companion object {
        private const val UNPACK_MARKER = "UNPACK:"

        private const val BUFFER_SIZE = 32 * 1024

        @JvmStatic
        private val NO_FILE_ATTRIBUTES = arrayOf<FileAttribute<*>>()

        @JvmStatic
        private val DIRECTORY_PERMISSIONS = EnumSet.of(OWNER_READ, OWNER_WRITE, OWNER_EXECUTE)

        @JvmStatic
        private val FILE_PERMISSIONS = EnumSet.of(OWNER_READ, OWNER_WRITE)
    }


    /**
     * JarFile URL
     */
    @Nullable
    private var url: URL? = null

    /**
     * 临时的未解压的文件夹
     *
     * @see Path
     */
    @Nullable
    private var tempUnpackDirectory: Path? = null

    constructor(file: File) : this(JarFile(file)) {
        this.url = file.toURI().toURL()
    }

    constructor(file: File, url: URL) : this(file) {
        this.url = url
    }

    /**
     * 获取到当前Jar归档文件当中的JarEntry列表的迭代器
     *
     * @return JarEntry列表的迭代器
     */
    override fun iterator(): Iterator<Archive.Entry> = EntryIterator(jarFile.iterator(), null, null)

    /**
     * 在关闭的方法当中, 需要去关闭[JarFile]
     *
     * @see JarFile.close
     */
    override fun close() = jarFile.close()

    /**
     * 获取当前JarFile归档文件的URL
     *
     * @return 如果指定了URL那么直接返回URL; 如果没有指定URL, 那么使用JarFile去获取URL
     */
    override fun getUrl() = url ?: jarFile.getUrl()

    /**
     * 获取当前Jar包的Archive归档文件内部嵌套的归档文件列表
     *
     * @param searchFilter ArchiveEntry的搜索的Filter
     * @param includeFilter 需要包含进来的Filter
     * @return 搜索到的内部嵌套的Archive归档文件列表
     */
    override fun getNestedArchives(
        searchFilter: Archive.EntryFilter,
        includeFilter: Archive.EntryFilter
    ): Iterator<Archive> = NestedArchiveIterator(this.jarFile.iterator(), searchFilter, includeFilter)

    /**
     * 获取当前Jar包的Manifest信息("MANIFEST.SF")
     *
     * @return 当前Jar包归档文件的Manifest(如果不存在Manifest的话, return null)
     */
    @Nullable
    override fun getManifest(): Manifest? = jarFile.manifest

    /**
     * 获取内部的Archive
     *
     * @param entry ArchiveEntry
     * @return Archive
     */
    protected open fun getNestedArchive(entry: Archive.Entry): Archive {
        val jarEntry = (entry as JarFileEntry).jarEntry

        // 如果JarEntry以"UNPACK:"开头的话
        if (jarEntry.comment?.startsWith(UNPACK_MARKER) == true) {
            return getUnpackedNestedArchive(jarEntry)
        }
        try {
            return JarFileArchive(jarFile.getNestedJarFile(jarEntry))
        } catch (ex: Exception) {
            throw IllegalStateException("无法根据给定的entry[${entry.getName()}]去创建JarFile", ex)
        }
    }

    /**
     * 获取没有被压缩的内部归档文件
     *
     * @param jarEntry JarEntry
     */
    @Throws(IOException::class)
    private fun getUnpackedNestedArchive(jarEntry: JarEntry): Archive {
        // 解析文件名的最后一个"/"之后的作为name
        var name = jarEntry.name
        if (name.lastIndexOf('/') != -1) {
            name = name.substring(name.lastIndexOf('/') + 1)
        }
        val path = getTempUnpackDirectory()!!.resolve(name)
        if (!Files.exists(path) || Files.size(path) != jarEntry.size) {
            unpack(jarEntry, path)
        }
        return JarFileArchive(path.toFile(), path.toUri().toURL())
    }

    private fun getTempUnpackDirectory(): Path? {
        if (tempUnpackDirectory == null) {
            val tempDirectory = Paths.get(System.getProperty("java.io.tmpdir"))
            tempUnpackDirectory = createUnpackDirectory(tempDirectory)
        }
        return tempUnpackDirectory
    }

    private fun createUnpackDirectory(parent: Path): Path? {
        var attempts = 0
        while (attempts++ < 1000) {
            val fileName = Paths.get(jarFile.name).fileName.toString()
            val unpackDirectory = parent.resolve(fileName + "-spring-boot-libs-" + UUID.randomUUID())
            try {
                createDirectory(unpackDirectory)
                return unpackDirectory
            } catch (ex: IOException) {
            }
        }
        throw IllegalStateException("Failed to create unpack directory in directory '$parent'")
    }

    @Throws(IOException::class)
    private fun unpack(entry: JarEntry, path: Path) {
        createFile(path)
        path.toFile().deleteOnExit()
        jarFile.getInputStream(entry).use { inputStream ->
            Files.newOutputStream(
                path, StandardOpenOption.WRITE,
                StandardOpenOption.TRUNCATE_EXISTING
            ).use { outputStream ->
                val buffer = ByteArray(BUFFER_SIZE)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
                outputStream.flush()
            }
        }
    }

    @Throws(IOException::class)
    private fun createDirectory(path: Path) {
        Files.createDirectory(path, *getFileAttributes(path.fileSystem, DIRECTORY_PERMISSIONS))
    }

    @Throws(IOException::class)
    private fun createFile(path: Path) {
        Files.createFile(path, *getFileAttributes(path.fileSystem, FILE_PERMISSIONS))
    }

    private fun getFileAttributes(
        fileSystem: FileSystem,
        ownerReadWrite: EnumSet<PosixFilePermission>
    ): Array<FileAttribute<*>> {
        return if (!fileSystem.supportedFileAttributeViews().contains("posix")) {
            NO_FILE_ATTRIBUTES
        } else arrayOf(PosixFilePermissions.asFileAttribute(ownerReadWrite))
    }

    /**
     * 抽象的遍历JarEntry的迭代器
     *
     * @param iterator JarEntry迭代器(java.util.jar.JarEntry)
     * @param searchFilter searchFilter
     * @param includeFilter includeFilter
     * @see JarEntry
     */
    private abstract class AbstractIterator<T>(
        private val iterator: Iterator<JarEntry>,
        private val searchFilter: Archive.EntryFilter?,
        private val includeFilter: Archive.EntryFilter?
    ) : Iterator<T> {

        /**
         * 迭代器正在迭代当中的的ArchiveEntry
         */
        private var current: Archive.Entry? = poll()

        /**
         * 是否还有下一个元素? (根据current是否为null去进行判断)
         *
         * @return 如果还有下一个元素, 那么return true; 否则return false
         */
        override fun hasNext() = this.current != null

        override fun next(): T {
            current ?: throw IllegalStateException("迭代器已经遍历完成了, 不允许继续使用next去获取元素")

            // 交给子类去进行类型转换
            val adapt = adapt(current!!)

            // current = current.next
            current = poll()
            return adapt
        }

        /**
         * 从迭代器当去取出来下一个元素, 去进行匹配;
         * 直到找到一个既符合searchFilter, 又符合includeFilter的Entry
         *
         * @return 如果找到了符合条件的元素, 那么return Entry; 否则return null
         */
        private fun poll(): Archive.Entry? {
            while (iterator.hasNext()) {
                val entry = JarFileEntry(iterator.next())
                if (searchFilter?.matches(entry) != false && includeFilter?.matches(entry) != false) {
                    return entry
                }
            }
            return null
        }

        /**
         * 将Entry去转换成为具体的类型
         *
         * @param entry entry
         * @return 转换之后的类型
         */
        protected abstract fun adapt(entry: Archive.Entry): T
    }

    /**
     * 内部归档文件的迭代器
     *
     * @param iterator JarEntry迭代器
     * @param searchFilter searchFilter
     * @param includeFilter includeFilter
     */
    private inner class NestedArchiveIterator(
        iterator: Iterator<JarEntry>,
        searchFilter: Archive.EntryFilter?,
        includeFilter: Archive.EntryFilter?
    ) : AbstractIterator<Archive>(iterator, searchFilter, includeFilter) {
        override fun adapt(entry: Archive.Entry) = getNestedArchive(entry)
    }

    /**
     * 归档文件的Entry的迭代器
     *
     * @param iterator JarEntry迭代器
     * @param searchFilter searchFilter
     * @param includeFilter includeFilter
     */
    private inner class EntryIterator(
        iterator: Iterator<JarEntry>,
        searchFilter: Archive.EntryFilter?,
        includeFilter: Archive.EntryFilter?
    ) : AbstractIterator<Archive.Entry>(iterator, searchFilter, includeFilter) {
        override fun adapt(entry: Archive.Entry) = entry
    }

    /**
     * 将Java当中的JarEntry桥接到Archive.Entry当中
     *
     * @param jarEntry JarEntry
     */
    private class JarFileEntry(val jarEntry: JarEntry) : Archive.Entry {
        override fun isDirectory(): Boolean = jarEntry.isDirectory
        override fun getName(): String = jarEntry.name
    }
}