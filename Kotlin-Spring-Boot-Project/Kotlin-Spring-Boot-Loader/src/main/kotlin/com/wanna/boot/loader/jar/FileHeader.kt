package com.wanna.boot.loader.jar

import java.util.zip.ZipEntry

/**
 * JarFile的文件头部信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/4
 */
interface FileHeader {


    /**
     * 返回压缩数据的方式(DEFLATED/STORED)
     *
     * @see ZipEntry.DEFLATED
     * @see ZipEntry.STORED
     * @return 压缩数据的方式
     */
    fun getMethod(): Int

    /**
     * 检查Header当中是否有包含给定的name的Header
     *
     * @param name headerName
     * @param suffix 额外的后缀(或者是0)
     * @return 如果包含了，那么return true；否则return false
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