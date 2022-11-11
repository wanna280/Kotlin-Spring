package com.wanna.boot.loader.jar

import com.wanna.boot.loader.data.RandomAccessData
import com.wanna.boot.loader.jar.Bytes.littleEndianValue
import java.io.IOException

/**
 * ZIP压缩包的结束标识，也就是EOCD("End of central directory record")，
 * Jdk当中的Jar包其实本质上就是一个ZIP包，因此它也会遵循ZIP包的规范，并且Jar包使用的是ZIP32格式
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/04
 * @param data 归档文件源数据
 */
internal class CentralDirectoryEndRecord(data: RandomAccessData) {
    companion object {
        /**
         * ZipFile的EOCD部分的最短长度为22
         */
        private const val MINIMUM_SIZE = 22
        private const val MAXIMUM_COMMENT_LENGTH = 0xFFFF
        private const val MAXIMUM_SIZE = MINIMUM_SIZE + MAXIMUM_COMMENT_LENGTH

        /**
         * ZIP归档的签名，固定值，为0x06054b50
         */
        private const val SIGNATURE = 0x06054b50
        private const val COMMENT_LENGTH_OFFSET = 20

        /**
         * 读取文件时的每块的大小，也就是ByteArray分配的空间大小
         */
        private const val READ_BLOCK_SIZE = 256
    }

    /**
     * 当前归档文件的Zip64End部分，如果它是一个Zip32文件的话，它为null
     */
    private val zip64End: Zip64End?

    /**
     * EOCD数据块(ByteArray)，可能读取的比较多(256)，只有末尾的部分才算是真正的EOCD
     */
    private var block: ByteArray

    /**
     * 从Block数据块的哪个位置开始才是EOCD？
     */
    private var offset: Int

    /**
     * 归档文件的EOCD的长度，默认(最小)的情况为22
     */
    private var size: Int

    init {
        // 从文件结束位置开始，往前一次性读取ZIP文件当中的256个字节(Byte)
        block = createBlockFromEndOfData(data, READ_BLOCK_SIZE)
        size = MINIMUM_SIZE
        offset = block.size - size
        while (!isValid) {
            size++
            if (size > block.size) {
                if (size >= MAXIMUM_SIZE || size > data.getSize()) {
                    throw IOException(
                        "Unable to find ZIP central directory records after reading $size bytes"
                    )
                }
                block = createBlockFromEndOfData(data, size + READ_BLOCK_SIZE)
            }
            offset = block.size - size
        }

        // 因为EOCD是在ZipFile的最后的部分，因此我们得往前去进行推移，才能得到EOCD的开始部分
        // 通过length-EOCD_Size，得到EOCD的开始部分的index，也就是startOfCentralDirectoryEndRecord
        val startOfCentralDirectoryEndRecord = data.getSize() - size

        // 解析Zip64的Locator，通过它获取到Zip64的偏移量
        val zip64Locator = Zip64Locator.find(data, startOfCentralDirectoryEndRecord)
        zip64End = if (zip64Locator != null) Zip64End(data, zip64Locator) else null
    }

    /**
     * 从给定的数据的最后开始，往前去读取一定大小的数据块成为一个ByteArray
     *
     * @param data 原始数据
     * @param size 要读取的块的大小
     * @return 从原始数据当中读取到的块数据
     */
    @Throws(IOException::class)
    private fun createBlockFromEndOfData(data: RandomAccessData, size: Int): ByteArray {
        // 要读取的块的大小，为min(length, dataSize)的最小值
        val length = data.getSize().coerceAtMost(size.toLong()).toInt()

        // 读取数据的开始位置为dataSize-length，也就是距离末尾length长度的位置
        return data.read(data.getSize() - length, length.toLong())
    }

    // Total size must be the structure size + comment
    private val isValid: Boolean
        get() {
            if (block.size < MINIMUM_SIZE || littleEndianValue(block, offset + 0, 4) != SIGNATURE.toLong()) {
                return false
            }
            // Total size must be the structure size + comment
            val commentLength = littleEndianValue(block, offset + COMMENT_LENGTH_OFFSET, 2)
            return size.toLong() == MINIMUM_SIZE + commentLength
        }

    /**
     * 获取整个归档文件的真正开始的位置(需要去掉Zip64End、Zip64EndLocator、CentralDirectoryLength、prefix特殊字段)
     *
     * @param data 支持去进行随机访问的ZIP(Jar)归档文件原始文件
     * @return 整个归档文件真正开始的位置offset(如果有prefix的话，那么跳过了prefix；如果没有prefix的话，return 0)
     */
    fun getStartOfArchive(data: RandomAccessData): Long {

        // EOCD的offset=12开始，读取4个字节，得到的就是所有CentralDirectory的总大小，也就是占用的Byte数量
        val length = littleEndianValue(block, offset + 12, 4)

        // CentralDirectory相对于ZipEntry开始位置的偏移量，如果是Zip32的话，直接从EOCD的offset=16开始读取4个字节就可以得到
        // 这部分其实就是ZipEntry的总长度的字节数量，对于正常情况下(没有prefix)的话specifiedOffset=actualOffset
        // 但是当Zip文件当中存在有prefix的情况下，actualOffset=prefixLength+specifiedOffset
        val specifiedOffset = zip64End?.centralDirectoryOffset ?: littleEndianValue(block, offset + 16, 4)

        // 计算Zip64End的大小(如果当前是Zip32的话，那么值为0就行)
        val zip64EndSize = zip64End?.getSize() ?: 0L

        // 计算Zip64Locator的大小，如果是Zip64的话，值为20；如果是Zip32的话，值为0
        val zip64LocSize = if (zip64End != null) Zip64Locator.ZIP64_LOCSIZE else 0

        // 计算得到真正的偏移量(这里计算方式为，数据总长度-CentralDirectory之后的总长度，最终得到的值是，prefix的长度+ZipEntry列表的总长度)
        // dataSize(文件总长度)-size(EOCD的长度)-length(CentralDirectory的长度)-zip64EndSize-zip64LocatorSize
        val actualOffset = data.getSize() - size - length - zip64EndSize - zip64LocSize

        // 正常情况下，actualOffset=specifiedOffset
        // 但是当存在有prefix的情况下，actualOffset=prefixLength+specifiedOffset，ZipEntry的真正的开始位置需要去掉prefixLength
        // 也就是actualOffset-specifiedOffset才算是真正的ZipEntry的开始位置
        return actualOffset - specifiedOffset
    }

    /**
     * 从给定的文件当中去寻找到"Central Directory"(中央目录)部分对应的字节数组，
     * 这里维护的就是Zip文件当中的ZipEntry列表当中的每个ZipEntry的头部信息
     *
     * @param data 原本Zip文件的数据(如果需要跳过prefix的话，这里是已经跳过的，不存在有prefix的的部分的数据)
     * @return 中央目录("Central Directory")的数据
     */
    fun getCentralDirectory(data: RandomAccessData): RandomAccessData {
        if (zip64End != null) {
            return zip64End.getCentralDirectory(data)
        }
        // 从EOCD的offset=16开始读取4个字节，就可以得到CentralDirectory相对于ZipEntry开始位置的偏移量
        // 其实得到的值也就是ZipEntry的长度，因为CentralDirectory在ZipEntry列表之后，因此我们需要计算一下ZipEntry的大小
        // 再去跳过这部分的数据，就可以得到CentralDirectory的开始位置了
        val offset = littleEndianValue(block, this.offset + 16, 4)

        // 从EOCD的offset=12开始读取4个字节，得到的就是CentralDirectory的长度(Byte)
        val length = littleEndianValue(block, this.offset + 12, 4)

        // 从CentralDirectory的开始位置，向后读取length的长度，得到的这部分的数据也就是CentralDirectory的数据
        return data.getSubsection(offset, length)
    }

    /**
     * 统计在这个ZIP文件当中的ZipEntry文件的数量
     * * 1.如果是Zip64的话，从Zip64End当中去获取Record数量
     * * 2.如果是Zip32的话，从EOCD的offset=10开始读取两个字节，就得到了Record数量(Zip32的文件数量最大只能到65534)
     */
    val numberOfRecords: Int
        get() {
            if (zip64End != null) {
                return zip64End.numberOfRecords
            }
            val numberOfRecords = littleEndianValue(block, offset + 10, 2)
            return numberOfRecords.toInt()
        }
    val comment: String
        get() {
            val commentLength = littleEndianValue(block, offset + COMMENT_LENGTH_OFFSET, 2).toInt()
            val comment = AsciiBytes(block, offset + COMMENT_LENGTH_OFFSET + 2, commentLength)
            return comment.toString()
        }

    /**
     * 是否是一个ZIP64的归档？如果解析出来了ZIP64End，那么说明是；否则说明不是
     *
     * @return 如果Zip64End不为空，那么return true；否则return false
     */
    val isZip64: Boolean
        get() = zip64End != null


    /**
     * 一个ZIP64的中央文件夹记录(Central Directory Record)，
     * 可以参考[https://pkware.cachefly.net/webdocs/casestudies/APPNOTE.TXT]的4.3.14章节
     */
    private class Zip64End constructor(data: RandomAccessData, private val locator: Zip64Locator) {

        companion object {
            private const val ZIP64_ENDTOT = 32 // total number of entries
            private const val ZIP64_ENDSIZ = 40 // central directory size in bytes
            private const val ZIP64_ENDOFF = 48 // offset of first CEN header
        }

        val centralDirectoryOffset: Long
        val centralDirectoryLength: Long

        /**
         * Return the number of entries in the zip64 archive.
         * @return the number of records in the zip
         */
        val numberOfRecords: Int

        init {
            val block = data.read(locator.zip64EndOffset, 56)
            centralDirectoryOffset = littleEndianValue(block, ZIP64_ENDOFF, 8)
            centralDirectoryLength = littleEndianValue(block, ZIP64_ENDSIZ, 8)
            numberOfRecords = littleEndianValue(block, ZIP64_ENDTOT, 8).toInt()
        }

        /**
         * Return the size of this zip 64 end of central directory record.
         * @return size of this zip 64 end of central directory record
         */
        fun getSize(): Long {
            return locator.zip64EndOffset
        }

        /**
         * Return the bytes of the "Central directory" based on the offset indicated in
         * this record.
         * @param data the source data
         * @return the central directory data
         */
        fun getCentralDirectory(data: RandomAccessData): RandomAccessData {
            return data.getSubsection(centralDirectoryOffset, centralDirectoryLength)
        }
    }

    /**
     * ZIP64的EOCD的定位器，用于完成ZIP64 End的定位
     * 可以参考[https://pkware.cachefly.net/webdocs/casestudies/APPNOTE.TXT]的4.3.15章节
     *
     * @param offset Zip64End的偏移量
     * @param block 从data当中读取到的Zip64End的块数据(长度为20的ByteArray)
     */
    private class Zip64Locator private constructor(private val offset: Long, block: ByteArray) {
        companion object {

            /**
             * Zip64的签名，固定值为0x07064b50
             */
            const val SIGNATURE = 0x07064b50

            /**
             * Zip64的Locator的长度，固定为20
             */
            const val ZIP64_LOCSIZE = 20 // locator size

            /**
             * Zip64End的偏移量
             */
            const val ZIP64_LOCOFF = 8 // offset of zip64 end

            @Throws(IOException::class)
            fun find(data: RandomAccessData, centralDirectoryEndOffset: Long): Zip64Locator? {

                // EOCD的Zip64的Locator的长度为20，因此我们往前去进行推移20个长度
                val offset = centralDirectoryEndOffset - ZIP64_LOCSIZE
                if (offset >= 0) {
                    // 从数据当中读取20个长度的数据块
                    val block = data.read(offset, ZIP64_LOCSIZE.toLong())

                    // 使用小端方式去读取4个字节的数据，检查签名是否是0x07064b50
                    // 签名是0x07064b50代表它是一个Zip64的文件
                    if (littleEndianValue(block, 0, 4) == SIGNATURE.toLong()) {
                        return Zip64Locator(offset, block)
                    }
                }
                return null
            }
        }


        /**
         * 如果确实是Zip64的话，那么使用小端方式从offset=8开始读取8个字节，得到的就是Zip64End的偏移量
         */
        val zip64EndOffset: Long = littleEndianValue(block, ZIP64_LOCOFF, 8)

        /**
         * 获取Zip64End的大小，从Zip64的开始位置(EOCD-20)，到offset这段，就算是Zip64End的长度
         *
         * @return Zip64的End长度
         */
        val zip64EndSize: Long
            get() = offset - zip64EndOffset
    }
}