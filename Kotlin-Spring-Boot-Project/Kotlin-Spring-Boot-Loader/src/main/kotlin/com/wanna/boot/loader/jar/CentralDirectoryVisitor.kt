package com.wanna.boot.loader.jar

import com.wanna.boot.loader.data.RandomAccessData

/**
 * 触发[CentralDirectoryParser]的回调方法，使用到的是访问者模式
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/4
 */
internal interface CentralDirectoryVisitor {
    fun visitStart(endRecord: CentralDirectoryEndRecord, centralDirectoryData: RandomAccessData)
    fun visitFileHeader(fileHeader: CentralDirectoryFileHeader, dataOffset: Long)
    fun visitEnd()
}