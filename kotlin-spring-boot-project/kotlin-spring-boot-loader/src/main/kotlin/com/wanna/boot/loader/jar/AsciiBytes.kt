package com.wanna.boot.loader.jar

import java.nio.charset.StandardCharsets
import javax.annotation.Nullable
import kotlin.experimental.and

/**
 * ASCII的字节序列
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/4
 */
class AsciiBytes(
    private val bytes: ByteArray,
    private val offset: Int = 0,
    private val length: Int = 0
) {

    companion object {
        private const val EMPTY_STRING = ""

        @JvmStatic
        private val INITIAL_BYTE_BITMASK = intArrayOf(0x7F, 0x1F, 0x0F, 0x07)
        private const val SUBSEQUENT_BYTE_BITMASK = 0x3F

        /**
         * 将给定的ByteArray, 去转换成为字符串
         *
         * @param bytes ByteArray
         * @return String
         */
        @JvmStatic
        fun toString(bytes: ByteArray): String {
            return String(bytes, StandardCharsets.UTF_8)
        }

        /**
         * 获取到给定的字符序列的hashCode
         *
         * @param charSequence 字符序列
         * @return 该字符序列的hashCode
         */
        @JvmStatic
        fun hashCode(charSequence: CharSequence): Int {
            // 如果是我们自定义的StringSequence, 那么直接调用它的hashCode方法去进行获取; 否则使用原生的hashCode去进行生成
            return if (charSequence is StringSequence) charSequence.hashCode() else charSequence.toString().hashCode()
        }

        /**
         * 将suffix后缀的hash值去累加到hashCode上
         *
         * @param hash 原始hash值
         * @param suffix 需要去进行混入的后缀
         * @return 混入之后计算得到的hashCode
         */
        @JvmStatic
        fun hashCode(hash: Int, suffix: Char): Int {
            return if ((suffix.code != 0)) (31 * hash + suffix.code) else hash
        }
    }

    private var string: String? = null

    /**
     * 提供一个基于string去进行构建的构造器
     */
    constructor(string: String, offset: Int = 0, length: Int = string.length) : this(
        string.toByteArray(StandardCharsets.UTF_8),
        offset,
        length
    ) {
        this.string = string
    }

    constructor(string: String) : this(string, 0, string.length)


    private var hash = 0

    fun length(): Int {
        return length
    }

    fun startsWith(prefix: AsciiBytes): Boolean {
        if (this === prefix) {
            return true
        }
        if (prefix.length > length) {
            return false
        }
        for (i in 0 until prefix.length) {
            if (this.bytes[i + offset] != prefix.bytes[i + prefix.offset]) {
                return false
            }
        }
        return true
    }

    fun endsWith(postfix: AsciiBytes): Boolean {
        if (this === postfix) {
            return true
        }
        if (postfix.length > length) {
            return false
        }
        for (i in 0 until postfix.length) {
            if (this.bytes[offset + (length - 1) - i] != postfix.bytes[postfix.offset + (postfix.length - 1)
                        - i]
            ) {
                return false
            }
        }
        return true
    }

    fun substring(beginIndex: Int): AsciiBytes {
        return substring(beginIndex, length)
    }

    fun substring(beginIndex: Int, endIndex: Int): AsciiBytes {
        val length = endIndex - beginIndex
        if (offset + length > this.bytes.size) {
            throw IndexOutOfBoundsException()
        }
        return AsciiBytes(this.bytes, offset + beginIndex, length)
    }

    fun matches(name: CharSequence, suffix: Char): Boolean {
        var charIndex = 0
        val nameLen = name.length
        val totalLen = nameLen + (if ((suffix.code != 0)) 1 else 0)
        var i = offset
        while (i < offset + length) {
            var b: Int = this.bytes[i].toInt()
            val remainingUtfBytes = getNumberOfUtfBytes(b) - 1
            b = b and INITIAL_BYTE_BITMASK[remainingUtfBytes]
            for (j in 0 until remainingUtfBytes) {
                b = (b shl 6) + (this.bytes[++i] and SUBSEQUENT_BYTE_BITMASK.toByte())
            }
            var c = getChar(name, suffix, charIndex++)
            if (b <= 0xFFFF) {
                if (c.code != b) {
                    return false
                }
            } else {
                if (c.code != ((b shr 0xA) + 0xD7C0)) {
                    return false
                }
                c = getChar(name, suffix, charIndex++)
                if (c.code != ((b and 0x3FF) + 0xDC00)) {
                    return false
                }
            }
            i++
        }
        return charIndex == totalLen
    }

    private fun getChar(name: CharSequence, suffix: Char, index: Int): Char {
        if (index < name.length) {
            return name[index]
        }
        return if (index == name.length) {
            suffix
        } else 0.toChar()
    }

    private fun getNumberOfUtfBytes(b: Int): Int {
        var b = b
        if ((b and 0x80) == 0) {
            return 1
        }
        var numberOfUtfBytes = 0
        while ((b and 0x80) != 0) {
            b = b shl 1
            numberOfUtfBytes++
        }
        return numberOfUtfBytes
    }

    override fun equals(@Nullable obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (this === obj) {
            return true
        }
        if (obj.javaClass == AsciiBytes::class.java) {
            val other = obj as AsciiBytes
            if (length == other.length) {
                for (i in 0 until length) {
                    if (this.bytes[offset + i] != other.bytes[other.offset + i]) {
                        return false
                    }
                }
                return true
            }
        }
        return false
    }

    override fun hashCode(): Int {
        var hash = hash
        if (hash == 0 && this.bytes.isNotEmpty()) {
            var i = offset
            while (i < offset + length) {
                var b: Int = this.bytes[i].toInt()
                val remainingUtfBytes = getNumberOfUtfBytes(b) - 1
                b = b and INITIAL_BYTE_BITMASK[remainingUtfBytes]
                for (j in 0 until remainingUtfBytes) {
                    b = (b shl 6) + (this.bytes[++i] and SUBSEQUENT_BYTE_BITMASK.toByte())
                }
                if (b <= 0xFFFF) {
                    hash = 31 * hash + b
                } else {
                    hash = 31 * hash + ((b shr 0xA) + 0xD7C0)
                    hash = 31 * hash + ((b and 0x3FF) + 0xDC00)
                }
                i++
            }
            this.hash = hash
        }
        return hash
    }

    override fun toString(): String {
        if (this.string == null) {
            if (length == 0) {
                this.string = EMPTY_STRING
            } else {
                this.string = String(this.bytes, offset, length, StandardCharsets.UTF_8)
            }
        }
        return this.string!!
    }
}