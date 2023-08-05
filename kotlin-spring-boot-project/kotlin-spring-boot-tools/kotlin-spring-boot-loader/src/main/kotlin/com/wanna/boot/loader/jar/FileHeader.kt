package com.wanna.boot.loader.jar

import java.util.zip.ZipEntry

/**
 * ZipFile(JarFile)的文件头部信息, 不管是ZipFile当中[CentralDirectoryFileHeader]
 * 还是[JarEntry]都共有的一部分属性属性的抽象出来的接口
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/4
 * @see CentralDirectoryFileHeader
 * @see JarEntry
 */
interface FileHeader {


    /**
     * 返回ZIP归档当中, 当前数据的压缩数据的方式(DEFLATED/STORED)
     * STORED(0)代表该文件没有被压缩, DEFLATED(8)代表该文件被压缩过
     *
     * @see ZipEntry.DEFLATED
     * @see ZipEntry.STORED
     * @return 压缩数据的方式(0/8)
     */
    fun getMethod(): Int

    /**
     * 检查Header当中是否有包含给定的name的Header
     *
     * @param name headerName
     * @param suffix 额外的后缀(或者是0)
     * @return 如果包含了, 那么return true; 否则return false
     */
    fun hasName(name: CharSequence, suffix: Char): Boolean

    /**
     * 获取归档文件内部的LocalFileHeader的偏移量
     *
     * @return LocalFileHeader的偏移量
     */
    fun getLocalHeaderOffset(): Long

    /**
     * 获取该Entry被压缩之后的大小
     *
     * @return 被压缩之后的大小
     */
    fun getCompressedSize(): Long

    /**
     * 获取该Entry没被压缩时的大小
     *
     * @return 没被压缩时的大小
     */
    fun getSize(): Long
}