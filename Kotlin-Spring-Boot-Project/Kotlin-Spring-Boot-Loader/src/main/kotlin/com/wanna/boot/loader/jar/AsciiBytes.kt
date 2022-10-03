package com.wanna.boot.loader.jar

import java.nio.charset.StandardCharsets
import kotlin.experimental.and

/**
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

        @JvmStatic
        fun toString(bytes: ByteArray?): String {
            return String((bytes)!!, StandardCharsets.UTF_8)
        }

        @JvmStatic
        fun hashCode(charSequence: CharSequence): Int {
            // We're compatible with String's hashCode()
            return if (charSequence is StringSequence) {
                // ... but save making an unnecessary String for StringSequence
                charSequence.hashCode()
            } else charSequence.toString().hashCode()
        }

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

    /**
     * ByteArray
     */


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
            if (this.bytes.get(i + offset) != prefix.bytes.get(i + prefix.offset)) {
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
            if (this.bytes.get(offset + (length - 1) - i) != postfix.bytes.get(
                    postfix.offset + (postfix.length - 1)
                            - i
                )
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

    override fun equals(obj: Any?): Boolean {
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
                    if (this.bytes.get(offset + i) != other.bytes.get(other.offset + i)) {
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