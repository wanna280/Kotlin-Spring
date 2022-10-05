package com.wanna.boot.loader.data

import java.io.*

/**
 * 基于文件的[RandomAccessData]的实现，实现对于文件的随机访问(按照index去访问文件)
 * 内部实现时通过组合一个jdk当中提供的[RandomAccessFile]来实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/3
 * @see RandomAccessFile
 * @see RandomAccessData
 */
class RandomAccessDataFile : RandomAccessData {

    /**
     * 提供文件访问的FileAccess对象
     */
    private val fileAccess: FileAccess

    /**
     * 读取文件的起始位置
     */
    private val offset: Long

    /**
     * 读取文件的长度
     */
    private val length: Long

    /**
     * 内部包装的去进行提供访问的文件对象
     *
     * @see FileAccess.file
     * @see File
     */
    val file: File
        get() = fileAccess.file


    /**
     * 根据给定的File去创建一个支持去进行随机访问的[RandomAccessDataFile]对象
     *
     * @param file 需要访问的文件
     */
    constructor(file: File) {
        fileAccess = FileAccess(file)
        offset = 0L
        length = file.length()
    }


    /**
     * 提供一个私有的构造器，供内部使用
     *
     * @param fileAccess FileAccess
     * @param offset offset
     * @param length length
     */
    private constructor(fileAccess: FileAccess, offset: Long, length: Long) {
        this.fileAccess = fileAccess
        this.offset = offset
        this.length = length
    }


    /**
     * 获取用于去访问当前文件的输入流
     *
     * @return InputStream
     */
    @Throws(IOException::class)
    override fun getInputStream(): InputStream = DataInputStream()

    /**
     * 创建一个只含有原始文件的一部分的[RandomAccessData]
     *
     * @param offset 要从原始文件的哪里开始读取？
     * @param length 要读取的长度？
     * @return 含有原始文件的一部分的RandomAccessData对象
     */
    override fun getSubsection(offset: Long, length: Long): RandomAccessData {
        if (offset < 0 || length < 0 || offset + length > this.length) {
            throw IndexOutOfBoundsException("访问文件越界")
        }
        return RandomAccessDataFile(fileAccess, this.offset + offset, length)
    }

    @Throws(IOException::class)
    override fun read(): ByteArray {
        return read(0, length)
    }

    @Throws(IOException::class)
    override fun read(offset: Long, length: Long): ByteArray {
        if (offset > this.length) {
            throw IndexOutOfBoundsException()
        }
        if (offset + length > this.length) {
            throw EOFException()
        }
        val bytes = ByteArray(length.toInt())
        read(bytes, offset, 0, bytes.size)
        return bytes
    }

    @Throws(IOException::class)
    private fun readByte(position: Long): Int {
        return if (position >= length) {
            -1
        } else fileAccess.readByte(offset + position)
    }

    @Throws(IOException::class)
    private fun read(bytes: ByteArray, position: Long, offset: Int, length: Int): Int {
        return if (position > this.length) {
            -1
        } else fileAccess.read(bytes, this.offset + position, offset, length)
    }

    /**
     * 返回当前文件的长度
     *
     * @return 当前文件的长度
     */
    override fun getSize() = this.length

    @Throws(IOException::class)
    fun close() = fileAccess.close()

    /**
     * [InputStream] implementation for the [RandomAccessDataFile].
     */
    private inner class DataInputStream : InputStream() {
        private var position = 0

        @Throws(IOException::class)
        override fun read(): Int {
            val read = readByte(position.toLong())
            if (read > -1) {
                moveOn(1)
            }
            return read
        }

        @Throws(IOException::class)
        override fun read(b: ByteArray): Int {
            return read(b, 0, b.size)
        }

        @Throws(IOException::class)
        override fun read(b: ByteArray, off: Int, len: Int): Int {
            return doRead(b, off, len)
        }

        /**
         * 执行真正的读取文件操作
         *
         * @param b ByteArray
         */
        @Throws(IOException::class)
        fun doRead(b: ByteArray, off: Int, len: Int): Int {
            if (len == 0) {
                return 0
            }
            val cappedLen = cap(len.toLong())
            return if (cappedLen <= 0) {
                -1
            } else moveOn(this@RandomAccessDataFile.read(b, position.toLong(), off, cappedLen)).toInt()
        }

        @Throws(IOException::class)
        override fun skip(n: Long): Long {
            return if (n <= 0) 0 else moveOn(cap(n))
        }

        @Throws(IOException::class)
        override fun available(): Int {
            return length.toInt() - position
        }

        private fun cap(n: Long): Int {
            return (length - position).coerceAtMost(n).toInt()
        }

        private fun moveOn(amount: Int): Long {
            position += amount
            return amount.toLong()
        }
    }

    /**
     * 提供随机文件访问的对象，通过组合jdk当中提供的[RandomAccessFile]去完成实现
     *
     * @param file 要去进行随机访问的文件
     */
    private class FileAccess(val file: File) {
        private val monitor = Any()
        private var randomAccessFile: RandomAccessFile? = null

        init {
            openIfNecessary()  // 打开文件
        }

        @Throws(IOException::class)
        fun read(bytes: ByteArray, position: Long, offset: Int, length: Int): Int {
            synchronized(monitor) {
                openIfNecessary()
                randomAccessFile!!.seek(position)
                return randomAccessFile!!.read(bytes, offset, length)
            }
        }

        private fun openIfNecessary() {
            if (randomAccessFile == null) {
                try {
                    randomAccessFile = RandomAccessFile(file, "r")
                } catch (ex: FileNotFoundException) {
                    throw IllegalArgumentException("文件 ${file.absolutePath} 必须存在")
                }
            }
        }

        @Throws(IOException::class)
        fun close() {
            synchronized(monitor) {
                if (randomAccessFile != null) {
                    randomAccessFile!!.close()
                    randomAccessFile = null
                }
            }
        }

        @Throws(IOException::class)
        fun readByte(position: Long): Int {
            synchronized(monitor) {
                openIfNecessary()
                randomAccessFile!!.seek(position)
                return randomAccessFile!!.read()
            }
        }
    }
}