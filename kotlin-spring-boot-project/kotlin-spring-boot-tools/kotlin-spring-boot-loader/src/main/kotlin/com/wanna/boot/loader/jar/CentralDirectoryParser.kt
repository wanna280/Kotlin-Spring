package com.wanna.boot.loader.jar

import com.wanna.boot.loader.data.RandomAccessData
import java.io.IOException


/**
 * 用于去解析Zip包(Jar包的)的"CentralDirectory"的解析器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/4
 */
internal class CentralDirectoryParser {
    companion object {

        /**
         * CentralDirectory当中的Header基础大小
         */
        private const val CENTRAL_DIRECTORY_HEADER_BASE_SIZE = 46
    }

    /**
     * 维护了提供对于CentralDirectory的访问的Visitor列表
     *
     * @see CentralDirectoryVisitor
     */
    private val visitors: MutableList<CentralDirectoryVisitor> = ArrayList()

    /**
     * 添加一个Visitor到当前的Parser当中, 在parse方法执行时, visit方法将会被自动执行
     *
     * @param visitor visitor
     * @return Visitor
     */
    fun <T : CentralDirectoryVisitor> addVisitor(visitor: T): T {
        visitors.add(visitor)
        return visitor
    }


    /**
     * 解析原始的ZIP(Jar)数据, 让它去成为一个真正的Archive归档文件(去掉前缀prefix部分的字节)
     *
     * @param data 原始数据
     * @param skipPrefixBytes 是否需要跳过ZIP文件最开始的prefix部分的数据
     * @return 不存在有任何prefix字节的真正Archive归档数据
     * @throws IOException 发生IO错误
     */
    @Throws(IOException::class)
    fun parse(data: RandomAccessData?, skipPrefixBytes: Boolean): RandomAccessData? {
        var newData = data ?: return null

        // 解析Zip文件的结束标识符EOCD
        val endRecord = CentralDirectoryEndRecord(newData)

        // 如果需要跳过prefix的字节的话, 那么需要进入下面的方法去进行跳过
        if (skipPrefixBytes) {
            newData = getArchiveData(endRecord, newData)
        }

        // 解析得到CentralDirectory的数据
        val centralDirectoryData = endRecord.getCentralDirectory(newData)

        // callback visitStart
        visitStart(endRecord, centralDirectoryData)

        // parse Entries(callback visitFileHeader)
        parseEntries(endRecord, centralDirectoryData)

        // callback visitEnd
        visitEnd()
        return newData
    }

    /**
     * 解析所有的JarEntry(ZipEntry)
     *
     * @param endRecord ZIP归档文件的结束标识符(EOCD)
     * @param centralDirectoryData CentralDirectory数据
     */
    @Throws(IOException::class)
    private fun parseEntries(endRecord: CentralDirectoryEndRecord, centralDirectoryData: RandomAccessData) {
        // 将CentralDirectory当中的数据全部读取出来放到ByteArray当中
        val bytes = centralDirectoryData.read(0, centralDirectoryData.size)
        val fileHeader = CentralDirectoryFileHeader()

        // FileHeader偏移量, 初始化为0, 代表只想了CentralDirectory当中的第一个FileHeader
        var dataOffset = 0

        // 遍历所有的CentralDirectoryFileHeader(EOCD当中已经存起来了的CentralDirectoryFileHeader数量), 去进行处理
        // 每个CentralDirectoryFileHeader对应了一个FileEntry(ZipEntry/JarEntry)
        for (i in 0 until endRecord.numberOfRecords) {

            // 根据CentralDirectory当中的一个元素的数据(CentralDirectoryFileHeader), 去加载到FileHeader
            fileHeader.load(bytes, dataOffset, null, 0, null)

            // visitFileHeader
            visitFileHeader(dataOffset.toLong(), fileHeader)

            // 计算偏移量, 方便下次遍历时, 根据offset去找到合适的FileHeader
            // 当前FileHeader的长度=46+fileNameSize+fileCommentSize+extraSize
            dataOffset += (CENTRAL_DIRECTORY_HEADER_BASE_SIZE + fileHeader.getName()!!
                .length() + fileHeader.getComment().length() + fileHeader.getExtra().size)
        }
    }

    /**
     * 获取真正的Archive归档文件的数据
     *
     * @param endRecord Zip文件的EOCD
     * @param data Archive文件的原始数据
     * @return 真正的Archive的数据(去掉了EOCD、Zip64Locator、Zip64End等部分的数据)
     */
    private fun getArchiveData(endRecord: CentralDirectoryEndRecord, data: RandomAccessData): RandomAccessData {
        // 获取Archive归档文件开始的位置(其实就是prefix的长度)
        val offset = endRecord.getStartOfArchive(data)

        // 如果offset=0的话, 那么说明不存在有prefix, 直接return data即可, 从0开始就是ZipEntry
        return if (offset == 0L) data

        // 如果offset不为0的话, 那么说明存在有prefix, 需要把prefix的长度去掉
        else data.getSubsection(offset, data.size - offset)
    }

    /**
     * visitStart, 需要回调所有的Visitor
     *
     * @param endRecord EOCD
     * @param centralDirectoryData CentralDirectoryData
     */
    private fun visitStart(endRecord: CentralDirectoryEndRecord, centralDirectoryData: RandomAccessData) {
        visitors.forEach { it.visitStart(endRecord, centralDirectoryData) }
    }

    /**
     * visitFileHeader, 需要回调所有的Visitor
     *
     * @param dataOffset 当前的FileHeader相对CentralDirectory的偏移量
     * @param fileHeader 当前正在访问的FileHeader
     */
    private fun visitFileHeader(dataOffset: Long, fileHeader: CentralDirectoryFileHeader) {
        visitors.forEach { it.visitFileHeader(fileHeader, dataOffset) }
    }

    /**
     * visitEnd, 需要回调所有的Visitor
     */
    private fun visitEnd() {
        visitors.forEach(CentralDirectoryVisitor::visitEnd)
    }
}