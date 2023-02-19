package com.wanna.boot.loader.data

import java.io.*

/**
 * 基于文件的[RandomAccessData]的实现, 实现对于文件的随机访问(按照index去访问文件)
 * 内部实现时通过组合一个jdk当中提供的[RandomAccessFile]来实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/3
 * @see RandomAccessFile
 * @see RandomAccessData
 *
 * @param fileAccess 提供文件的随机访问的FileAccess对象, 像数组一样的方式去进行文件的访问, 基于offset&length用于去切取一部分数据
 * @param offset 读取文件的起始位置
 * @param length 读取文件的长度
 */
class RandomAccessDataFile(private val fileAccess: FileAccess, private val offset: Long, private val length: Long) :
    RandomAccessData {

    /**
     * 内部包装的去进行提供访问的文件对象
     *
     * @see FileAccess.file
     */
    val file: File = fileAccess.file

    /**
     * 获取当前文件的长度
     */
    override val size: Long = this.length


    /**
     * 根据给定的[File]文件去创建一个支持去进行随机访问的[RandomAccessDataFile]对象
     *
     * @param file 需要访问的文件
     */
    constructor(file: File) : this(FileAccess(file), 0L, file.length())


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
     * @param offset 要从原始文件的哪里开始读取?
     * @param length 要读取的长度?
     * @return 含有原始文件的一部分的RandomAccessData对象
     */
    override fun getSubsection(offset: Long, length: Long): RandomAccessData {
        if (offset < 0 || length < 0 || offset + length > this.length) {
            throw IndexOutOfBoundsException("访问文件越界")
        }
        return RandomAccessDataFile(fileAccess, this.offset + offset, length)
    }

    /**
     * 读取文件当中的全部内容
     *
     * @return 文件当中的全部内容的ByteArray
     */
    @Throws(IOException::class)
    override fun read(): ByteArray {
        return read(0, length)
    }

    /**
     * 读取文件当中的部分的内容
     *
     * @param offset 要去进行读取的文件的偏移量
     * @param length 要去读取的文件的长度
     * @return 读取到的文件当中的内容ByteArray
     */
    @Throws(IOException::class)
    override fun read(offset: Long, length: Long): ByteArray {
        if (offset > this.length) {
            throw IndexOutOfBoundsException()
        }

        // 如果offset+length长度越界
        if (offset + length > this.length) {
            throw EOFException()
        }

        // 申请预期长度的空间的ByteArray
        val bytes = ByteArray(length.toInt())

        // 将数据读取放入到bytes当中
        read(bytes, offset, 0, bytes.size)
        return bytes
    }

    /**
     * 读取给定的位置的字节
     *
     * @param position 要去读取的位置position
     */
    @Throws(IOException::class)
    private fun readByte(position: Long): Int {
        return if (position >= length) -1 else fileAccess.readByte(offset + position)
    }

    @Throws(IOException::class)
    private fun read(bytes: ByteArray, position: Long, offset: Int, length: Int): Int {
        return if (position > this.length) -1 else fileAccess.read(bytes, this.offset + position, offset, length)
    }

    /**
     * 关闭正在去进行访问的文件
     *
     * @see FileAccess.close
     * @throws IOException 如果关闭文件的过程当中遇到了异常
     */
    @Throws(IOException::class)
    fun close() = fileAccess.close()

    /**
     * 提供对于[RandomAccessDataFile]数据的读取的输入流, 用于JarFile去进行输入流的获取
     */
    private inner class DataInputStream : InputStream() {
        private var position = 0L

        @Throws(IOException::class)
        override fun read(): Int {
            val read = readByte(position)
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
            } else moveOn(this@RandomAccessDataFile.read(b, position, off, cappedLen)).toInt()
        }

        @Throws(IOException::class)
        override fun skip(n: Long): Long {
            return if (n <= 0) 0 else moveOn(cap(n))
        }

        @Throws(IOException::class)
        override fun available(): Int {
            return length.toInt() - position.toInt()
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
     * 提供随机文件访问的对象, 通过组合jdk当中提供的[RandomAccessFile]去完成实现对于文件的随机访问
     *
     * @param file 要去进行随机访问的文件
     */
    class FileAccess(val file: File) {

        /**
         * 操作文件时需要用到的锁对象, 因为涉及到seek寻址操作, 因此会存在有并发安全问题
         */
        private val monitor = Any()

        /**
         * JDK当中提供的原生的[RandomAccessFile], 去提供对于文件的随机访问
         */
        private var randomAccessFile: RandomAccessFile? = null

        init {
            openIfNecessary()  // 打开文件
        }

        /**
         * 尝试将给定的文件去进行打开
         *
         * @throws FileNotFoundException 如果读取文件失败
         */
        @Throws(FileNotFoundException::class)
        private fun openIfNecessary() {
            if (randomAccessFile == null) {
                try {
                    randomAccessFile = RandomAccessFile(file, "r")
                } catch (ex: FileNotFoundException) {
                    throw IllegalArgumentException("file ${file.absolutePath} is not exists")
                }
            }
        }

        /**
         * 读取文件当中一部分数据, 并存放到给定的ByteArray当中
         *
         * @param bytes 存放读取的数据的ByteArray, 最终的数据将会存放到这里
         * @param position 文件的殉职偏移量
         * @param offset 读取文件的偏移量
         * @param length 要去读取的文件的长度
         * @return 成功读取到ByteArray当中的字节数量
         */
        @Throws(IOException::class)
        fun read(bytes: ByteArray, position: Long, offset: Int, length: Int): Int {
            synchronized(monitor) {
                openIfNecessary()
                randomAccessFile!!.seek(position)
                return randomAccessFile!!.read(bytes, offset, length)
            }
        }

        /**
         * 关闭正在去进行读取的文件
         */
        @Throws(IOException::class)
        fun close() {
            synchronized(monitor) {
                if (randomAccessFile != null) {
                    randomAccessFile!!.close()
                    randomAccessFile = null
                }
            }
        }

        /**
         * 读取给定的位置的数据
         *
         * @param position 要去读取数据的位置
         * @return 读取到的给定位置的文件内容
         */
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