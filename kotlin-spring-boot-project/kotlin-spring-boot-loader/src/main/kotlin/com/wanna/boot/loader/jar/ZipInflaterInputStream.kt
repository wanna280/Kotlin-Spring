package com.wanna.boot.loader.jar

import java.io.EOFException
import java.io.IOException
import java.io.InputStream
import java.util.zip.Inflater
import java.util.zip.InflaterInputStream

/**
 * 提供压缩文件的读取的输入流
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/4
 */
internal class ZipInflaterInputStream(inputStream: InputStream, private var available: Int) :
    InflaterInputStream(inputStream, Inflater(true), getInflaterBufferSize(available.toLong())) {
    private var extraBytesWritten = false

    @Throws(IOException::class)
    override fun available(): Int {
        return if (available < 0) {
            super.available()
        } else available
    }

    @Throws(IOException::class)
    override fun read(b: ByteArray, off: Int, len: Int): Int {
        val result = super.read(b, off, len)
        if (result != -1) {
            available -= result
        }
        return result
    }

    @Throws(IOException::class)
    override fun close() {
        super.close()
        inf.end()
    }

    @Throws(IOException::class)
    override fun fill() {
        try {
            super.fill()
        } catch (ex: EOFException) {
            if (extraBytesWritten) {
                throw ex
            }
            len = 1
            buf[0] = 0x0
            extraBytesWritten = true
            inf.setInput(buf, 0, len)
        }
    }

    companion object {
        @JvmStatic
        private fun getInflaterBufferSize(size: Long): Int {
            var sz = size + 2
            sz = if (sz > 65536) 8192 else sz
            sz = if (sz <= 0) 4096 else sz
            return sz.toInt()
        }
    }
}