package com.wanna.boot.loader.jar

import com.wanna.boot.loader.data.RandomAccessData

/**
 * 在解析一个ZipFile的EOCD和CentralDirectory时需要被触发[CentralDirectoryParser]的回调方法，使用到的是访问者模式
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/4
 * @see CentralDirectoryParser.visitors
 */
internal interface CentralDirectoryVisitor {

    /**
     * visitStart，在解析出来EOCD和CentralDirectory时会被自动回调
     *
     * @param endRecord ZipFile的EOCD
     * @param centralDirectoryData CentralDirectoryData
     * @see CentralDirectoryParser.visitStart
     */
    fun visitStart(endRecord: CentralDirectoryEndRecord, centralDirectoryData: RandomAccessData)

    /**
     * visitFileHeader，在解析出来EOCD和CentralDirectory之后，
     * 会解析出来所有的CentralDirectoryFileHeader，去回调visitFileHeader方法完成处理
     *
     * @param fileHeader CentralDirectoryFileHeader
     * @param dataOffset CentralDirectoryFileHeader相对于CentralDirectory起始位置的相对偏移量
     * @see CentralDirectoryParser.visitFileHeader
     */
    fun visitFileHeader(fileHeader: CentralDirectoryFileHeader, dataOffset: Long)

    /**
     * visitEnd，在解析完成时，会被自动回调
     *
     * @see CentralDirectoryParser.visitStart
     */
    fun visitEnd()
}