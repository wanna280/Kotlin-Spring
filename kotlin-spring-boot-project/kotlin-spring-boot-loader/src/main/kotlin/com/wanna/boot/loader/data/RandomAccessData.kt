package com.wanna.boot.loader.data

import java.io.IOException
import java.io.InputStream
import java.lang.IndexOutOfBoundsException
import kotlin.jvm.Throws

/**
 * 支持去通过随机访问的Data, 也就是支持去把整个数据当做数组的方式去进行读取;
 * 只需要根据offset/length就可以去读取到一定区间的范围内的数据
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/3
 */
interface RandomAccessData {

    /**
     * 获取用于数据的读取的输入流
     *
     * @return 读取数据的输入流
     */
    @Throws(IOException::class)
    fun getInputStream(): InputStream

    /**
     * 将整个数据去读取成为ByteArray
     *
     * @return 数据内容的ByteArray
     */
    @Throws(IOException::class)
    fun read(): ByteArray

    /**
     * 从指定的偏移量开始去读取数据的一部分
     *
     * @param offset 数据开始读取的偏移量
     * @param length 要读取的数据的长度
     * @return 读取到的数据的内容ByteArray
     */
    @Throws(IOException::class)
    fun read(offset: Long, length: Long): ByteArray

    /**
     * 获取数据的一部分, 并封装成为RandomAccessData对象
     *
     * @param offset 数据开始读取的偏移量
     * @param length 要读取的数据的长度
     * @return 读取到的数据的内容
     * @throws IndexOutOfBoundsException 如果offset/length小于0或者offset+length超过了数据的长度
     */
    @Throws(IndexOutOfBoundsException::class)
    fun getSubsection(offset: Long, length: Long): RandomAccessData

    /**
     * 获取数据的长度
     *
     * @return 数据长度
     */
    fun getSize(): Long
}