package com.wanna.boot.loader.jar

import com.wanna.boot.loader.data.RandomAccessData
import com.wanna.boot.loader.jar.AsciiBytes.Companion.hashCode
import com.wanna.boot.loader.jar.Bytes.littleEndianValue
import com.wanna.boot.loader.jar.CentralDirectoryFileHeader.Companion.fromRandomAccessData
import com.wanna.boot.loader.jar.JarEntryCertification.Companion.from
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.jar.Attributes
import java.util.jar.JarInputStream
import java.util.zip.ZipEntry
import javax.annotation.Nullable


/**
 * 维护了[JarFile]内部的[JarEntry]列表, 提供了[JarFile]内部的[JarEntry]的访问
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/4
 * @param jarFile 当前JarEntries所在的JarFile
 * @param filter 用于过滤JarFile当中的JarEntry的过滤器, 只有符合条件的情况下, 才会被收集起来
 */
internal class JarFileEntries(private val jarFile: JarFile, @Nullable private val filter: JarEntryFilter?) :
    CentralDirectoryVisitor, Iterable<JarEntry> {

    companion object {

        @JvmStatic
        private val NO_VALIDATION = Runnable {}

        /**
         * META-INF的前缀
         */
        private const val META_INF_PREFIX = "META-INF/"

        @JvmStatic
        private val MULTI_RELEASE = Attributes.Name("Multi-Release")
        private const val BASE_VERSION = 8

        /**
         * 获取当前运行时的Java版本
         */
        @JvmStatic
        private val RUNTIME_VERSION = Runtime.version().feature()

        /**
         * ZIPEntry的LocalFileHeader长度, 长度为30
         */
        private const val LOCAL_FILE_HEADER_SIZE = 30L

        /**
         * 左斜杠的常量
         */
        private const val SLASH = '/'

        /**
         * 没有后缀的标识
         */
        private const val NO_SUFFIX = 0.toChar()
        const val ENTRY_CACHE_SIZE = 25

        @JvmStatic
        private fun swap(array: IntArray, i: Int, j: Int) {
            val temp = array[i]
            array[i] = array[j]
            array[j] = temp
        }

        @JvmStatic
        private fun swap(array: LongArray, i: Int, j: Int) {
            val temp = array[i]
            array[i] = array[j]
            array[j] = temp
        }
    }

    /**
     * 当前[JarFileEntries]对应的CentralDirectory数据
     */
    private lateinit var centralDirectoryData: RandomAccessData

    /**
     * 维护了当前JarEntries当中的的JarEntry的数量
     */
    var size = 0
        private set  // 私有化Setter

    /**
     * 三个数组同步初始化, 并基于hashCodes数组去完成排序, 去提供对于各个JarEntry的索引的建立
     */
    private lateinit var hashCodes: IntArray
    private lateinit var positions: IntArray
    private lateinit var centralDirectoryOffsets: Offsets
    private lateinit var certifications: Array<JarEntryCertification>

    /**
     * 判断当前Jar包是否是一个多发行版的Jar包?
     * 如果Manifest当中存在有"Multi-Release"的话, 就属于是多发行版Jar包
     */
    private val isMultiReleaseJar: Boolean
        get() =
            try {
                val manifest = jarFile.manifest
                if (manifest == null) {
                    false
                } else {
                    val attributes = manifest.mainAttributes
                    attributes.containsKey(MULTI_RELEASE)
                }
            } catch (ex: IOException) {
                false
            }

    /**
     * JarEntry的缓存, Key是index, Value-FileHeader
     */
    private val entriesCache = Collections.synchronizedMap(
        object : LinkedHashMap<Int, FileHeader>(16, 0.75f, true) {
            override fun removeEldestEntry(eldest: Map.Entry<Int, FileHeader>) = size >= ENTRY_CACHE_SIZE
        })

    /**
     * 获取用于迭代[JarEntry]列表的迭代器
     *
     * @return 迭代[JarEntry]列表的迭代器
     */
    override fun iterator(): Iterator<JarEntry> = EntryIterator(NO_VALIDATION)

    /**
     * 获取用于迭代JarEntry的迭代器
     *
     * @param validator 检验迭代过程的Validator
     * @return 迭代JarEntry的迭代器
     */
    fun iterator(validator: Runnable): Iterator<JarEntry> = EntryIterator(validator)

    /**
     * 判断当前[JarFileEntries]当中是否包含有给定的文件的entryName的[JarEntry]
     *
     * @param name entryName(fileName)
     * @return 如果包含这样的JarEntry的话, return true; 否则return false
     */
    fun containsEntry(name: CharSequence): Boolean = getEntry(name, FileHeader::class.java, true) != null

    /**
     * 根据文件名去获取到JarEntry
     *
     * @param name entryName
     * @return 获取到的JarEntry(获取不到return null)
     */
    @Nullable
    fun getEntry(name: CharSequence): JarEntry? = getEntry(name, JarEntry::class.java, true)

    /**
     * 根据文件名, 去找到该文件的EntryData的输入流
     *
     * @param name 文件名
     * @return 该文件EntryData的输入流
     */
    @Nullable
    @Throws(IOException::class)
    fun getInputStream(name: String): InputStream? = getInputStream(getEntry(name, FileHeader::class.java, false))

    /**
     * 根据文件名, 去找到该文件的EntryData的输入流
     *
     * @param entry FileHeader
     * @return 该文件EntryData的输入流
     */
    @Nullable
    @Throws(IOException::class)
    fun getInputStream(@Nullable entry: FileHeader?): InputStream? {
        entry ?: return null
        var inputStream = getEntryData(entry).getInputStream()

        // 如果该JarEntry已经被压缩过了, 那么创建一个ZipInflaterInputStream去提供压缩文件的读取
        if (entry.getMethod() == ZipEntry.DEFLATED) {
            inputStream = ZipInflaterInputStream(inputStream, entry.getSize().toInt())
        }
        return inputStream
    }

    /**
     * 根据文件的entryName, 去找到合适文件的数据(EntryData)
     *
     * @param name 文件的entryName
     * @return 根据该文件entryName找到的文件数据(EntryData)
     */
    @Nullable
    @Throws(IOException::class)
    fun getEntryData(name: String): RandomAccessData? {
        // 首先, 我们根据name去找到FileHeader
        val entry = getEntry(name, FileHeader::class.java, false) ?: return null

        // 接着, 我们根据FileHeader去找到真正的EntryData
        return getEntryData(entry)
    }

    /**
     * visitStart, 在解析得到EOCD和CentralDirectory时, 会被自动回调用于交由你去保存一些上下文信息
     *
     * @param endRecord EOCD
     * @param centralDirectoryData CentralDirectoryData
     */
    override fun visitStart(endRecord: CentralDirectoryEndRecord, centralDirectoryData: RandomAccessData) {
        // 根据EOCD当中的得到的CentralDirectoryFileHeader数量(ZipEntry/JarEntry数量), 去创建出来合适长度的数组
        this.hashCodes = IntArray(endRecord.numberOfRecords)
        this.positions = IntArray(endRecord.numberOfRecords)

        // 保存CentralDirectoryData
        this.centralDirectoryData = centralDirectoryData

        // CentralDirectory的偏移量列表
        this.centralDirectoryOffsets = Offsets.from(endRecord)
    }

    /**
     * 在解析CentralDirectory时, 会遍历所有的FileHeader, 去完成回调
     *
     * @see CentralDirectoryParser.parse
     * @param fileHeader FileHeader
     * @param dataOffset 该FileHeader相对于CentralDirectory的起始地址的偏移量
     */
    override fun visitFileHeader(fileHeader: CentralDirectoryFileHeader, dataOffset: Long) {
        // 从FileHeader当中去解析得到文件名(并根据Filter去完成过滤)
        val name = applyFilter(fileHeader.getName()!!)

        // 如果该文件需要去保存起来的话, 那么我们可以根据文件名, 去建立起来该文件的索引
        name?.let { add(it, dataOffset) }
    }

    /**
     * 根据文件名, 去建立起来索引
     *
     * @param name 文件名
     * @param dataOffset 该文件的FileHeader相对于CentralDirectory的起始地址的偏移量
     */
    private fun add(name: AsciiBytes, dataOffset: Long) {
        hashCodes[size] = name.hashCode()
        centralDirectoryOffsets[size] = dataOffset
        positions[size] = size
        size++
    }

    /**
     * visitEnd
     */
    override fun visitEnd() {
        // 对hashCodes/centralDirectoryOffsets/positions三个数组去完成排序
        sort(0, size - 1)

        // 创建一个新的positions数组, 去替换掉原来的positions数组
        val positions = positions
        this.positions = IntArray(positions.size)

        // ????
        for (i in 0 until size) {
            this.positions[positions[i]] = i
        }
    }

    /**
     * 对hashCode数组的[left, right]这些位置的元素, 去完成排序("Quick Sort");
     * 从代码上看起来是简单的hashCode的元素的排序,
     * 实际上会对hashCodes/centralDirectoryOffsets/positions三个数组同时去进行排序;
     * 对于centralDirectoryOffsets/positions的排序使用的规则都是按照hashCode的顺序,
     * 在执行完成排序之后, 三者的位置对应关系仍旧完全相同, 根据index可以完全对应好三者
     *
     * @param left leftIndex
     * @param right rightIndex
     */
    private fun sort(left: Int, right: Int) {
        if (left < right) {
            val pivot = hashCodes[left + (right - left) / 2]
            var i = left
            var j = right
            while (i <= j) {
                while (hashCodes[i] < pivot) {
                    i++
                }
                while (hashCodes[j] > pivot) {
                    j--
                }
                if (i <= j) {
                    swap(i, j)
                    i++
                    j--
                }
            }
            if (left < j) {
                sort(left, j)
            }
            if (right > i) {
                sort(i, right)
            }
        }
    }

    /**
     * 对于三个数组当中的i和j对应的元素, 全部执行去执行交换
     *
     * @param i i
     * @param j j
     */
    private fun swap(i: Int, j: Int) {
        swap(hashCodes, i, j)
        centralDirectoryOffsets.swap(i, j)
        swap(positions, i, j)
    }

    /**
     * 根据[FileHeader]去获取到该[JarEntry]的数据
     *
     * @param entry entry FileHeader
     * @return 读取到的JarEntry数据
     */
    @Throws(IOException::class)
    private fun getEntryData(entry: FileHeader): RandomAccessData {
        // 对于"aspectjrt-1.7.4.jar"在LocalHeader到真正的数据之间有一个不同的ext字段, 我们需要去重新读取一下, 在这里去跳过这些ext字段
        val data = jarFile.data

        // ZIP压缩文件的LocalHeader长度为30, 我们在这里将LocalHeader读取出来
        val localHeader = data.read(entry.getLocalHeaderOffset(), LOCAL_FILE_HEADER_SIZE)

        // 从localHeader读取name的长度
        val nameLength = littleEndianValue(localHeader, 26, 2)

        // 从localHeader读取extra的长度
        val extraLength = littleEndianValue(localHeader, 28, 2)

        // 从ZipEntry当中去读取文件的内容
        // entry.getLocalHeaderOffset计算的是当前JarEntry的localHeader的偏移量, 也就是该ZipEntry(含localHeader的基地址)
        // 我们这里需要去跳过name和extra这两部分, 于是通过以下的计算：
        // 1.通过"30(LOCAL_FILE_HEADER_SIZE)+nameLength+extraLength"去跳过LocalHeader的部分, 去计算得到原始文件存放的的起始位置
        // 2.通过entry.compressedSize计算得到该文件的长度
        return data.getSubsection(
            entry.getLocalHeaderOffset() + LOCAL_FILE_HEADER_SIZE + nameLength + extraLength,
            entry.getCompressedSize()
        )
    }

    /**
     * 根据文件名entryName&type去获取到FileHeader
     *
     * @param name 文件名entryName
     * @param type type(CentralDirectoryFileHeader/JarEntry)
     * @param cacheEntry 是否需要把搜索结果添加到entryCache当中?
     * @return FileHeader
     *
     * @param T FileHeader类型(CentralDirectoryFileHeader/JarEntry)
     */
    @Nullable
    private fun <T : FileHeader> getEntry(name: CharSequence, type: Class<T>, cacheEntry: Boolean): T? {
        // 根据name和type去获取到对应的FileHeader
        val entry: T? = doGetEntry(name, type, cacheEntry, null)

        // 如果它不是一个META-INF的FileHeader, 并且是个多发行版的Jar包?
        if (!isMetaInfEntry(name) && isMultiReleaseJar) {
            var version = RUNTIME_VERSION
            val nameAlias = if (entry is JarEntry) entry.asciiBytesName else AsciiBytes(name.toString())
            while (version > BASE_VERSION) {
                val versionedEntry = doGetEntry("META-INF/versions/$version/$name", type, cacheEntry, nameAlias)
                if (versionedEntry != null) {
                    return versionedEntry
                }
                version--
            }
        }
        return entry
    }

    /**
     * 当前给定的这个文件名entryName是否是"META-INF/"路径下的Entry?
     *
     * @param name 文件名entryName
     * @return 如果以该entryName是以"META-INF/"开头, return true; 否则return false
     */
    private fun isMetaInfEntry(name: CharSequence): Boolean = name.toString().startsWith(META_INF_PREFIX)

    /**
     * 根据fileName和type, 去获取到JarEntry
     *
     * @param name fileName
     * @param type type(CentralDirectoryFileHeader/JarEntry)
     * @param cacheEntry 是否需要把搜索结果添加到entryCache当中?
     */
    @Nullable
    private fun <T : FileHeader> doGetEntry(
        name: CharSequence, type: Class<T>, cacheEntry: Boolean,
        @Nullable nameAlias: AsciiBytes?
    ): T? {

        // 生成该name的hashCode
        var hashCode = hashCode(name)
        // 根据name执行getEntry去获取到FileHeader
        var entry = getEntry(hashCode, name, NO_SUFFIX, type, cacheEntry, nameAlias)

        // 如果没有获取到的话, 那么我们尝试在name后面添加"/"去进行获取
        if (entry == null) {
            // 生成该name和"/"混合之后的hashCode
            hashCode = hashCode(hashCode, SLASH)
            // 根据name和"/"执行getEntry去获取到FileHeader
            entry = getEntry(hashCode, name, SLASH, type, cacheEntry, nameAlias)
        }
        return entry
    }

    /**
     * 执行getEntry, 根据文件名和hashCode获取到CentralDirectoryFileHeader
     *
     * @param hashCode 文件名的hashCode
     * @param name 文件名
     * @param type type(CentralDirectoryFileHeader/JarEntry)
     * @param cacheEntry 是否需要把搜索结果添加到entryCache当中?
     * @param nameAlias nameAlias
     */
    @Nullable
    private fun <T : FileHeader> getEntry(
        hashCode: Int, name: CharSequence, suffix: Char, type: Class<T>,
        cacheEntry: Boolean, @Nullable nameAlias: AsciiBytes?
    ): T? {

        // 根据hashCode, 从数组当中去获取到第一个hashCode为该值的元素(使用二分查找)
        var index = getFirstIndex(hashCode)

        // 从该位置开始向后寻找, 遍历所有的hashCode都完全匹配的元素
        while ((index in (0 until size)) && (hashCodes[index] == hashCode)) {
            // 根据index去进行获取到FileHeader
            val entry = getEntry(index, type, cacheEntry, nameAlias)

            // 如果name匹配的话, 那么return
            if (entry.hasName(name, suffix)) {
                return entry
            }
            index++
        }

        // 如果没有找到合适的结果, 那么return null
        return null
    }


    /**
     * 根据entryName(fileName), 去获取到该Entry所在的位置
     *
     * @param name fileName
     * @return 该fileName所在的数组的位置(如果找不到, 那么return -1)
     */
    private fun getEntryIndex(name: CharSequence): Int {
        val hashCode = hashCode(name)
        var index = getFirstIndex(hashCode)
        while (index in 0 until size && hashCodes[index] == hashCode) {
            val candidate = getEntry(index, FileHeader::class.java, false, null)
            if (candidate.hasName(name, NO_SUFFIX)) {
                return index
            }
            index++
        }
        return -1
    }

    /**
     * 根据index, 去获取到对应位置的FileHeader
     *
     * @param index index
     * @param type type
     * @param cacheEntry 是否需要把搜索结果添加到entryCache当中?
     * @param nameAlias nameAlias
     * @return FileHeader(CentralDirectoryFileHeader/JarEntry)
     */
    @Suppress("UNCHECKED_CAST")
    private fun <T : FileHeader> getEntry(index: Int, type: Class<T>, cacheEntry: Boolean, nameAlias: AsciiBytes?): T {
        return try {
            val offset = centralDirectoryOffsets[index]
            val cached = entriesCache[index]

            // 如果缓存当中没有的话, 那么我们构建出来一个CentralDirectoryFileHeader对象
            var entry = cached ?: fromRandomAccessData(centralDirectoryData, offset, filter)

            // 如果得到的结果是CentralDirectoryFileHeader, 但是你需要的是JarEntry的话
            // 那么我们把结果给你包装成为JarEntry返回给你即可...
            if (CentralDirectoryFileHeader::class.java == entry.javaClass && type == JarEntry::class.java) {
                entry = JarEntry(jarFile, index, (entry as CentralDirectoryFileHeader), nameAlias)
            }

            // 如果需要进行缓存的话, 我们我们在这里添加到缓存当中
            if (cacheEntry && cached !== entry) {
                entriesCache[index] = entry
            }

            // 类型转换, 并返回
            entry as T
        } catch (ex: Exception) {
            throw IllegalStateException(ex)
        }
    }

    /**
     * 获取[hashCodes]数组当中, 第一个元素为hashCode的元素的索引值
     *
     * @param hashCode 要去进行搜寻的hashCode
     * @return 从hashCodes数组当中搜索得到的hashCode相等的第一个元素(使用二分查找的方式), 如果找不到hashCode匹配的元素, 那么return -1
     */
    private fun getFirstIndex(hashCode: Int): Int {
        var index = Arrays.binarySearch(hashCodes, 0, size, hashCode)

        // 如果搜索不到结果, 那么return -1
        if (index < 0) {
            return -1
        }

        // 如果该位置并不是第一个hashCode匹配的元素, 那么尝试往前去进行寻找
        while (index > 0 && hashCodes[index - 1] == hashCode) {
            index--
        }
        return index
    }

    /**
     * 清空JarEntries的Cache缓存
     */
    fun clearCache() = entriesCache.clear()

    /**
     * 将给定的name去应用Filter完成转换
     *
     * @param name name
     * @return 经过Filter转换之后的name(主要是针对文件夹的情况, 需要把前缀给切割掉, 比如"BOOT-INF/classes/com/wanna/App.class"转换成为"com/wanna/App.class")
     */
    @Nullable
    private fun applyFilter(name: AsciiBytes): AsciiBytes? = if (filter != null) filter.apply(name) else name

    @Suppress("UNCHECKED_CAST")
    @Throws(IOException::class)
    fun getCertification(entry: JarEntry): JarEntryCertification {
        var certifications: Array<JarEntryCertification?>? = certifications as Array<JarEntryCertification?>?
        if (certifications == null) {
            certifications = arrayOfNulls(size)
            JarInputStream(jarFile.data.getInputStream()).use { certifiedJarStream ->
                var certifiedEntry: java.util.jar.JarEntry
                while (certifiedJarStream.nextJarEntry.also { certifiedEntry = it } != null) {
                    // Entry must be closed to trigger a read and set entry certificates
                    certifiedJarStream.closeEntry()
                    val index = getEntryIndex(certifiedEntry.name)
                    if (index != -1) {
                        certifications[index] = from(certifiedEntry)
                    }
                }
            }
            this.certifications = certifications as Array<JarEntryCertification>
        }
        val certification = certifications[entry.index]
        return certification ?: JarEntryCertification.NONE
    }


    /**
     * 提供遍历一个Jar包当中的[JarEntry]列表的迭代器
     *
     * @param validator 完成检验的Validator(Runnable)
     */
    inner class EntryIterator constructor(private val validator: Runnable) : Iterator<JarEntry> {

        /**
         * 当前迭代器所处元素的位置, 根据和size去进行对比, 就知道是否还有下一个元素了
         *
         * @see JarFileEntries.size
         */
        private var index = 0

        init {
            validator.run()
        }

        override fun hasNext(): Boolean {
            validator.run()
            return index < size
        }

        override fun next(): JarEntry {
            validator.run()
            if (!hasNext()) {
                throw NoSuchElementException()
            }
            val entryIndex = positions[index++]
            return getEntry(entryIndex, JarEntry::class.java, false, null)
        }
    }

    /**
     * 提供CentralDirectory的偏移量的管理, 正常的Zip文件将会采用IntArray去进行实现,
     * 对于Zip64文件将会使用LongArray去进行实现, 并且会消耗更多的Memory内存
     *
     * @see ZipOffsets
     * @see Zip64Offsets
     */
    private interface Offsets {
        /**
         * set和get, 提供运算符重载
         */
        operator fun set(index: Int, value: Long)
        operator fun get(index: Int): Long

        /**
         * 交换Offsets数组当中的i和j的位置
         *
         * @param i i
         * @param j j
         */
        fun swap(i: Int, j: Int)

        companion object {
            /**
             * 根据EOCD去决定Zip文件的类型, 从而去创建Offsets, 创建Zip64Offsets/ZipOffsets
             *
             * @param endRecord EOCD
             * @return Offsets(Zip64Offsets/ZipOffsets)
             */
            @JvmStatic
            fun from(endRecord: CentralDirectoryEndRecord): Offsets =
                if (endRecord.isZip64) Zip64Offsets(endRecord.numberOfRecords) else ZipOffsets(endRecord.numberOfRecords)
        }
    }

    /**
     * 针对于正常的Zip文件的[Offsets]的实现
     *
     * @param size ZipEntry的数量(CentralDirectory的数量)
     */
    private class ZipOffsets(size: Int) : Offsets {
        private val offsets = IntArray(size)
        override fun swap(i: Int, j: Int) = swap(offsets, i, j)
        override fun get(index: Int): Long = offsets[index].toLong()
        override fun set(index: Int, value: Long) {
            offsets[index] = value.toInt()
        }
    }

    /**
     * 针对于Zip64文件的[Offsets]的实现
     *
     * @param size ZipEntry的数量(CentralDirectory的数量)
     */
    private class Zip64Offsets(size: Int) : Offsets {
        private val offsets: LongArray = LongArray(size)
        override fun swap(i: Int, j: Int) = swap(offsets, i, j)
        override fun get(index: Int): Long = offsets[index]
        override fun set(index: Int, value: Long) {
            offsets[index] = value
        }
    }
}