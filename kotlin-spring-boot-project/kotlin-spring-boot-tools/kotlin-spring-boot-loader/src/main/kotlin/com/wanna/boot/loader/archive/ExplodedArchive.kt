package com.wanna.boot.loader.archive

import java.io.File
import java.io.FileInputStream
import java.net.MalformedURLException
import java.net.URL
import java.util.*
import java.util.jar.Manifest
import javax.annotation.Nullable

/**
 * 这是一个被解压之后得到的Java归档文件(War Exploded Archive),
 * 将整个Jar包解压之后得到一个大的文件夹, 将这个目录下的全部文件作为候选的Entry去进行处理
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/26
 *
 * @param root 要去作为Archive的root文件夹
 * @param recursive 是否需要去进行递归搜索root下的所有文件去作为Archive归档文件的一部分(如果为false, 将会只去进行一层的搜索)
 */
open class ExplodedArchive @JvmOverloads constructor(private val root: File, private val recursive: Boolean = true) :
    Archive {
    companion object {

        /**
         * 对于列出所有的文件时, "."/".."都应该跳过
         */
        @JvmStatic
        private val SKIPPED_NAMES = setOf(".", "..")
    }

    init {
        if (!root.isDirectory || !root.exists()) {
            throw IllegalStateException("不合法的根文件夹, 可能root不是一个文件夹, 也可能是该文件夹不存在")
        }
    }

    /**
     * Manifest的文件("META-INF/MANIFEST.MF")
     */
    private val manifestFile: File = getManifestFile(root)

    /**
     * Manifest
     */
    @Nullable
    private var manifest: Manifest? = null

    /**
     * 根据root路径, 去找到Manifest对应的文件路径("META-INF/MANIFEST.MF")
     *
     * @param root root File
     * @return Manifest文件
     */
    private fun getManifestFile(root: File): File {
        val metaInf = File(root, "META-INF")
        return File(metaInf, "MANIFEST.MF")
    }

    /**
     * 获取该Archive归档文件所在的文件夹的URL
     *
     * @return root URL
     */
    override fun getUrl(): URL = this.root.toURI().toURL()

    /**
     * 当前Archive归档文件是一个WarExploded, 值为true
     */
    override val exploded: Boolean = true

    /**
     * 获取当前Exploded Archive的Manifest("META-INF/MANIFEST.MF")
     *
     * @return Manifest(如果不存在Manifest的话, return null)
     */
    @Nullable
    override fun getManifest(): Manifest? {
        if (this.manifest == null && this.manifestFile.exists()) {
            val inputStream = FileInputStream(manifestFile)
            inputStream.use {
                this.manifest = Manifest(inputStream)
            }
        }
        return this.manifest
    }

    /**
     * 获取到当前Archive归档文件当中的Entry列表的迭代器
     *
     * @return Entry迭代器
     */
    override fun iterator(): Iterator<Archive.Entry> {
        return EntryIterator(root, recursive, null, null)
    }

    /**
     * 获取当前Archive归档文件其内部嵌套的Archive归档文件列表
     *
     * @param searchFilter 搜索的EntryFilter
     * @param includeFilter 需要进行包含的EntryFilter
     * @return 当前Archive归档文件内的所有符合EntryFilter要求的嵌套Archive归档文件
     */
    override fun getNestedArchives(
        searchFilter: Archive.EntryFilter,
        includeFilter: Archive.EntryFilter
    ): Iterator<Archive> {
        return ArchiveIterator(root, recursive, searchFilter, includeFilter)
    }

    /**
     * 对于[File]下的目录去转换成为[FileEntry], 并去对[FileEntry]进行提供BFS广度优先遍历的方式去进行迭代
     *
     * @param T Archive/ArchiveEntry, 根据子类的泛型类型去进行决定
     * @param root Archive归档根目录
     * @param recursive 是否需要去进行递归处理?
     * @param searchFilter 用于搜索Entry的searchFilter
     * @param includeFilter 需要去进行包含的Entry的检查的includeFilter
     */
    private abstract class AbstractIterator<T>(
        private val root: File,
        private val recursive: Boolean,
        @Nullable private val searchFilter: Archive.EntryFilter?,
        @Nullable private val includeFilter: Archive.EntryFilter?
    ) : Iterator<T> {

        companion object {
            /**
             * 对于处理文件的顺序去进行排序的比较器
             */
            @JvmStatic
            private val entryComparator: Comparator<File> = Comparator.comparing(File::getAbsolutePath)
        }

        /**
         * root Url, 也就是当前Archive的根目录所在的文件
         */
        private val rootUrl: String = root.toURI().path

        /**
         * 当前正在处理的[FileEntry]
         */
        @Nullable
        private var current: FileEntry? = null

        /**
         * 维护遍历所有的文件的栈, 提供对于内部的文件的迭代, 采用BFS的方式去进行迭代和处理
         */
        private val stack = LinkedList<Iterator<File>>()

        init {
            // 初始化对象时, 先给栈当中去放入一些root目录下的文件列表
            stack.add(listFiles(root))

            // fixed: 在这里去进行poll, 避免产生poll时, stack还未完成初始化工作
            this.current = poll()
        }

        /**
         * 如果当前正在去进行处理的[FileEntry]不为空, 说明还有下一个元素; 如果为null则说明已经遍历完了
         *
         * @return true if current is not null
         */
        override fun hasNext(): Boolean = current != null

        /**
         * 获取下一个需要去进行处理的元素
         *
         * @return 下一个需要去进行处理的元素
         * @throws NoSuchElementException 如果已经不存在下一个元素的话
         */
        override fun next(): T {
            val currentEntry = this.current ?: throw NoSuchElementException()

            // 在获取到当前Entry之后, 尝试去获取下一个Entry
            this.poll()

            // 将FileEntry去转换成为合适类型的对象(Archive/ArchiveEntry)
            return adapt(currentEntry)
        }

        /**
         * poll, 用于去获取下一个要去进行处理的[FileEntry]
         *
         * @return 下一个要去进行处理的FileEntry, 不存在下一个FileEntry的话, 那么return null
         */
        @Nullable
        private fun poll(): FileEntry? {
            while (stack.isNotEmpty()) {
                while (stack.peek().hasNext()) {

                    // fixed: 在这个循环当中遍历的都是stack.peek元素, 不应该使用poll
                    val file = stack.peek().next()

                    // 如果是fileName"."或者是"..", 那么跳过
                    if (SKIPPED_NAMES.contains(file.name)) {
                        continue
                    }
                    val fileEntry = getFileEntry(file)

                    // 如果当前FileEntry目录下还能列举出来更多的目录的话, 那么把这些先添加到队列头部, 可以快速被处理到
                    // 实现的效果是: 对于单层目录之间, 采用广度优先遍历; 对于多层目录之间, 采用的是深度优先遍历, 对于全局来说就是BFS广度优先遍历
                    if (isListable(fileEntry)) {
                        this.stack.addFirst(listFiles(file))
                    }

                    // 如果当前FileEntry符合要求的话, 返回当前FileEntry
                    if (includeFilter == null || includeFilter.matches(fileEntry)) {
                        return fileEntry
                    }

                }

                // 如果处理完当前文件夹了, 那么需要把当前元素从栈当中去进行移除
                this.stack.poll()
            }
            return null
        }

        /**
         * 检查给定的[FileEntry]对应的文件夹下, 是否还可以去进行列举出来更多可用的[FileEntry]?
         *
         * * 1.只有在[recursive]为true的情况下, 才允许被递归列举, 不然只能去列举出来一层的目录
         * * 2.只有在[includeFilter]和[searchFilter]都匹配成功的情况下, 才能去进行列举下一层的目录
         *
         * @param entry Entry
         * @return 如果还能进行列举更多[FileEntry], return true; 否则return false
         */
        private fun isListable(entry: FileEntry): Boolean {
            return entry.isDirectory
                    && (this.recursive || entry.file.parentFile.equals(this.root))
                    && (this.searchFilter == null || this.searchFilter.matches(entry))
                    && (this.includeFilter == null || this.includeFilter.matches(entry))
        }

        /**
         * 将给定的[File], 去转换成为一个[FileEntry]
         *
         * @param file File
         * @return FileEntry
         */
        private fun getFileEntry(file: File): FileEntry {
            val uri = file.toURI()

            // FileEntryName, 去掉root的前缀
            val name = uri.path.substring(this.rootUrl.length)
            try {
                return FileEntry(name, file, uri.toURL())
            } catch (ex: MalformedURLException) {
                throw IllegalStateException(ex)
            }
        }


        /**
         * 列举出来给定的[File]目录下的所有的文件的列表
         *
         * @param file file
         * @return 该目录下的所有文件列表的迭代器
         */
        private fun listFiles(file: File): Iterator<File> {
            val listFiles = file.listFiles() ?: return Collections.emptyIterator()
            listFiles.sortWith(entryComparator)
            return listFiles.iterator()
        }

        /**
         * 将[FileEntry]去进行类型转换, 具体需要转换成为的类型, 交给子类去进行执行
         *
         * @param entry FileEntry
         * @return 转换之后得到的结果
         */
        protected abstract fun adapt(entry: FileEntry): T
    }

    /**
     * 提供ArchiveEntry的迭代的迭代器
     */
    private class EntryIterator(
        root: File,
        recursive: Boolean,
        @Nullable searchFilter: Archive.EntryFilter?,
        @Nullable includeFilter: Archive.EntryFilter?
    ) : AbstractIterator<Archive.Entry>(root, recursive, searchFilter, includeFilter) {
        override fun adapt(entry: FileEntry): Archive.Entry = entry
    }

    /**
     * 提供对于root下嵌套的Archive归档文件的迭代的迭代器
     */
    private class ArchiveIterator(
        root: File,
        recursive: Boolean,
        @Nullable searchFilter: Archive.EntryFilter?,
        @Nullable includeFilter: Archive.EntryFilter?
    ) : AbstractIterator<Archive>(root, recursive, searchFilter, includeFilter) {

        /**
         * 对[FileEntry]去进行转换成为[Archive]
         *
         * * 1.如果[FileEntry]是一个文件夹的话, 那么包装成为[ExplodedArchive], 递归处理
         * * 2.如果[FileEntry]是一个文件的话, 那么包装成为[SimpleJarFileArchive]
         *
         * @param entry FileEntry
         * @return 置换之后的Archive
         */
        override fun adapt(entry: FileEntry): Archive {
            return if (entry.file.isDirectory) ExplodedArchive(entry.file) else SimpleJarFileArchive(entry)
        }
    }

    /**
     * FileEntry, 封装文件系统下的一个文件成为ArchiveEntry
     *
     * @param name entryName(去掉了root作为前缀, 只保留相对root的相对路径)
     * @param file 要去进行封装的File
     * @param url FileURL
     */
    private data class FileEntry(override val name: String, val file: File, val url: URL) : Archive.Entry {
        override val isDirectory: Boolean
            get() = file.isDirectory
    }

    /**
     * 将单个文件对应的[FileEntry]去包装成为[Archive]
     *
     * @param fileEntry FileEntry
     */
    private class SimpleJarFileArchive(fileEntry: FileEntry) : Archive {

        /**
         * ArchiveURL
         */
        private val url: URL = fileEntry.url

        override fun iterator(): Iterator<Archive.Entry> = Collections.emptyIterator()

        override fun getUrl(): URL = this.url

        @Nullable
        override fun getManifest(): Manifest? = null

        override fun getNestedArchives(
            searchFilter: Archive.EntryFilter,
            includeFilter: Archive.EntryFilter
        ): Iterator<Archive> = Collections.emptyIterator()
    }
}