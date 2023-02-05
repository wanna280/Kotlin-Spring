package com.wanna.boot.loader.tools

import java.io.IOException
import java.io.OutputStream

/**
 * JarEntry的Writer
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/5
 */
fun interface EntryWriter {

    /**
     * 执行JarEntry的写入
     *
     * @param outputStream JarEntry的输出流
     * @throws IOException 如果写入JarEntry失败
     */
    @Throws(IOException::class)
    fun write(outputStream: OutputStream)

    /**
     * 计算将要写入的内容的大小, -1代表未知
     *
     * @return content size
     */
    fun size() = -1
}