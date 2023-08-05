package com.wanna.boot.loader.jar

import com.wanna.boot.loader.data.RandomAccessData
import java.io.IOException
import java.util.zip.ZipEntry

/**
 * 描述一个ZIP文件的中央文件夹的FileHeader, 也就是"Central directory file header record"
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/4
 */
class CentralDirectoryFileHeader(
    private var header: ByteArray,
    private var headerOffset: Int,
    private var name: AsciiBytes?,
    private var extra: ByteArray,
    private var comment: AsciiBytes,
    private var localHeaderOffset: Long
) : FileHeader {

    /**
     * 提供一个内部使用的无参数构造器, 方便完成构建
     */
    constructor() : this(ByteArray(0), -1, AsciiBytes(""), ByteArray(0), AsciiBytes(""), -1)

    companion object {
        private val SLASH = AsciiBytes("/")

        /**
         * Central Directory Header的最小长度为46
         */
        private const val CENTRAL_DIRECTORY_HEADER_MINIMUM_LENGTH = 46L

        private val NO_EXTRA = byteArrayOf()

        private val NO_COMMENT = AsciiBytes("")

        /**
         * 从RandomAccessData去构建CentralDirectoryFileHeader
         *
         * @return FileHeader
         */
        @JvmStatic
        fun fromRandomAccessData(
            randomAccessData: RandomAccessData,
            offset: Long,
            jarEntryFilter: JarEntryFilter?
        ): CentralDirectoryFileHeader {
            val fileHeader = CentralDirectoryFileHeader()
            // 从文件当中, 根据偏移量offset, 去读取46个字节的长度, 并解析到CentralDirectoryFileHeader当中
            val bytes = randomAccessData.read(offset, CENTRAL_DIRECTORY_HEADER_MINIMUM_LENGTH)
            fileHeader.load(bytes, 0, randomAccessData, offset, jarEntryFilter)
            return fileHeader
        }
    }

    @Throws(IOException::class)
    fun load(
        data: ByteArray,
        dataOffset: Int,
        variableData: RandomAccessData?,
        variableOffset: Long,
        filter: JarEntryFilter?
    ) {
        // 加载CentralDirectoryFileHeader当中固定的部分
        var data = data
        var dataOffset = dataOffset
        header = data
        headerOffset = dataOffset
        val compressedSize = Bytes.littleEndianValue(data, dataOffset + 20, 4)
        val uncompressedSize = Bytes.littleEndianValue(data, dataOffset + 24, 4)
        val nameLength = Bytes.littleEndianValue(data, dataOffset + 28, 2)
        val extraLength = Bytes.littleEndianValue(data, dataOffset + 30, 2)
        val commentLength = Bytes.littleEndianValue(data, dataOffset + 32, 2)
        val localHeaderOffset = Bytes.littleEndianValue(data, dataOffset + 42, 4)

        // 加载CentralDirectoryFileHeader当中可能会有变化的部分
        dataOffset += 46
        if (variableData != null) {
            data = variableData.read(variableOffset + 46, nameLength + extraLength + commentLength)
            dataOffset = 0
        }
        name = AsciiBytes(data, dataOffset, nameLength.toInt())
        if (filter != null) {
            name = filter.apply(name!!)
        }
        extra = NO_EXTRA
        comment = NO_COMMENT
        if (extraLength > 0) {
            extra = ByteArray(extraLength.toInt())
            System.arraycopy(data, (dataOffset + nameLength).toInt(), extra, 0, extra.size)
        }
        this.localHeaderOffset = getLocalHeaderOffset(compressedSize, uncompressedSize, localHeaderOffset, extra)
        if (commentLength > 0) {
            comment = AsciiBytes(data, (dataOffset + nameLength + extraLength).toInt(), commentLength.toInt())
        }
    }

    @Throws(IOException::class)
    private fun getLocalHeaderOffset(
        compressedSize: Long,
        uncompressedSize: Long,
        localHeaderOffset: Long,
        extra: ByteArray
    ): Long {
        if (localHeaderOffset != 0xFFFFFFFFL) {
            return localHeaderOffset
        }
        var extraOffset = 0
        while (extraOffset < extra.size - 2) {
            val id = Bytes.littleEndianValue(extra, extraOffset, 2).toInt()
            val length = Bytes.littleEndianValue(extra, extraOffset, 2).toInt()
            extraOffset += 4
            if (id == 1) {
                var localHeaderExtraOffset = 0
                if (compressedSize == 0xFFFFFFFFL) {
                    localHeaderExtraOffset += 4
                }
                if (uncompressedSize == 0xFFFFFFFFL) {
                    localHeaderExtraOffset += 4
                }
                return Bytes.littleEndianValue(extra, extraOffset + localHeaderExtraOffset, 8)
            }
            extraOffset += length
        }
        throw IOException("Zip64 Extended Information Extra Field not found")
    }

    /**
     * 返回ZIP归档当中, 当前数据的压缩数据的方式(DEFLATED/STORED)
     * STORED(0)代表该文件没有被压缩, DEFLATED(8)代表该文件被压缩过
     *
     * @see ZipEntry.DEFLATED
     * @see ZipEntry.STORED
     * @return 压缩数据的方式(0/8)
     */
    override fun getMethod() = Bytes.littleEndianValue(header, headerOffset + 10, 2).toInt()

    /**
     * 判断当前FileHeader当中是否存在有name? 
     */
    override fun hasName(name: CharSequence, suffix: Char): Boolean = this.name?.matches(name, suffix) ?: false

    /**
     * 获取FileHeader的CRC校验码
     */
    fun getCrc(): Long = Bytes.littleEndianValue(header, headerOffset + 16, 4)

    /**
     * 获取LocalHeader Offset
     *
     * @return LocalHeader Offset
     */
    override fun getLocalHeaderOffset() = localHeaderOffset

    /**
     * 获取当前文件名
     *
     * @return 文件名
     */
    fun getName(): AsciiBytes? = this.name

    /**
     * 判断当前文件是否是一个文件夹? 
     */
    fun isDirectory(): Boolean = this.name?.endsWith(SLASH) ?: false

    fun getExtra(): ByteArray = this.extra

    fun hasExtra(): Boolean = this.extra.isNotEmpty()

    fun getComment(): AsciiBytes = this.comment

    /**
     * 获取该Entry被压缩之后的大小
     *
     * @return 被压缩之后的大小
     */
    override fun getCompressedSize() = Bytes.littleEndianValue(header, headerOffset + 20, 4)

    /**
     * 获取该Entry被压缩之前的大小
     *
     * @return 被压缩之前的大小
     */
    override fun getSize() = Bytes.littleEndianValue(header, headerOffset + 24, 4)
}