package com.wanna.framework.util

import java.io.OutputStream

/**
 * IO流的工具类
 */
object StreamUtils {

    /**
     * 将字节输出拷贝到输出流当中
     *
     * @param bytes 要去进行拷贝的字节数组
     * @param outStream 要写出的输出流
     */
    @JvmStatic
    fun copy(bytes: ByteArray, outStream: OutputStream) {
        outStream.write(bytes)
        outStream.flush()
    }
}