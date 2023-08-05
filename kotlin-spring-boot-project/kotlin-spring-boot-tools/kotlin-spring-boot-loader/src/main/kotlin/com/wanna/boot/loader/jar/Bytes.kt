package com.wanna.boot.loader.jar


/**
 * 提供关于ByteArray的读取的工具方法
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/4
 */
internal object Bytes {
    /**
     * 使用小端方式去读取
     *
     * @param bytes 要读取的ByteArray
     * @param offset 从哪里开始读取?
     * @param length 要读取的长度
     * @return 读取到的内容
     */
    @JvmStatic
    fun littleEndianValue(bytes: ByteArray, offset: Int, length: Int): Long {
        var value: Long = 0
        for (i in length - 1 downTo 0) {
            value = value shl 8 or ((bytes[offset + i].toInt() and 0xFF).toLong())
        }
        return value
    }
}