package com.wanna.boot.loader.jar

/**
 * 自定义的字符串序列
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/4
 */
class StringSequence(
    source: String,
    start: Int = 0,
    end: Int = source.length
) : CharSequence {
    private val source: String
    private val start: Int
    private val end: Int
    private var hash = 0

    init {
        if (start < 0) {
            throw StringIndexOutOfBoundsException(start)
        }
        if (end > source.length) {
            throw StringIndexOutOfBoundsException(end)
        }
        this.source = source
        this.start = start
        this.end = end
    }

    constructor(source: String) : this(source, 0, source.length)

    override val length: Int
        get() = end - start

    override fun get(index: Int) = source[index]

    fun subSequence(start: Int) = subSequence(start, length)

    override fun subSequence(startIndex: Int, endIndex: Int): StringSequence {
        val subSequenceStart = this.start + startIndex
        val subSequenceEnd = this.start + endIndex
        if (subSequenceStart > this.end) {
            throw StringIndexOutOfBoundsException(startIndex)
        }
        if (subSequenceEnd > this.end) {
            throw StringIndexOutOfBoundsException(endIndex)
        }
        return if (startIndex == 0 && subSequenceEnd == this.end) {
            this
        } else StringSequence(
            source,
            subSequenceStart,
            subSequenceEnd
        )
    }

    override fun isEmpty(): Boolean = length == 0

    fun indexOf(ch: Char): Int {
        return source.indexOf(ch, start) - start
    }

    fun indexOf(str: String?): Int {
        return source.indexOf(str!!, start) - start
    }

    fun indexOf(str: String?, fromIndex: Int): Int {
        return source.indexOf(str!!, start + fromIndex) - start
    }

    @JvmOverloads
    fun startsWith(prefix: String, offset: Int = 0): Boolean {
        val prefixLength = prefix.length
        val length = length
        return if (length - prefixLength - offset < 0) {
            false
        } else source.startsWith(prefix, start + offset)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is CharSequence) {
            return false
        }
        var n = length
        if (n != other.length) {
            return false
        }
        var i = 0
        while (n-- != 0) {
            if (get(i) != other[i]) {
                return false
            }
            i++
        }
        return true
    }

    override fun hashCode(): Int {
        var hash = hash
        if (hash == 0 && length > 0) {
            for (i in start until end) {
                hash = 31 * hash + source[i].code
            }
            this.hash = hash
        }
        return hash
    }

    override fun toString(): String {
        return source.substring(start, end)
    }
}