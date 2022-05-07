package com.wanna.framework.core.util

/**
 * 这是一个String的工具类
 */
object StringUtils {
    // 空的String数组
    private val EMPTY_STRING_ARRAY = emptyArray<String>()

    /**
     * 判断一个字符串是否有文本，判断长度是否为0
     */
    @JvmStatic
    fun hasText(str: String?): Boolean {
        return str != null && str.isNotBlank()
    }

    /**
     * 判断str在startIndex位置之后的部分字符是否和substring匹配
     *
     * @param str 要匹配的源(src)字符串
     * @param startIndex 匹配的字符串的开始位置，也就是说明匹配的字符串为str[startIndex]之后的部分的字符串
     * @param substring 匹配的目标(target)字符串
     */
    @JvmStatic
    fun substringMatch(str: CharSequence, startIndex: Int, substring: CharSequence): Boolean {
        // 如果str当中字符数量已经不够了，return
        if (startIndex + substring.length > str.length) {
            return false
        }
        // 匹配，str在startIndex之后的元素和substring去进行匹配
        for (i in substring.indices) {
            if (str[i + startIndex] != substring[i]) {
                return false
            }
        }
        return true
    }

    @JvmStatic
    fun collectionToCommaDelimitedString(collection: Collection<String?>?): String {
        return collectionToCommaDelimitedString(collection, ",")
    }

    @JvmStatic
    fun collectionToCommaDelimitedString(collection: Collection<String?>?, delim: String): String {
        return collectionToCommaDelimitedString(collection, delim, prefix = "", suffix = "")
    }

    @JvmStatic
    fun collectionToCommaDelimitedString(
        collection: Collection<String?>?, delim: String, prefix: String, suffix: String
    ): String {
        if (collection == null || collection.isEmpty()) {
            return ""
        }
        val builder = StringBuilder()
        val iterator = collection.iterator()
        while (iterator.hasNext()) {
            builder.append(prefix).append(iterator.next()).append(suffix)
            if (iterator.hasNext()) {
                builder.append(delim)
            }
        }
        return builder.toString()
    }

    /**
     * 将字符串转为字符串数组(采用,作为分隔符)
     */
    @JvmStatic
    fun commaDelimitedListToStringArray(str: String?): Array<String> {
        return commaDelimitedListToStringArray(str, ",")
    }

    /**
     * 将字符串转为字符串数组(采用delim作为分隔符)
     * @param delim 分隔符
     */
    @JvmStatic
    fun commaDelimitedListToStringArray(str: String?, delim: String?): Array<String> {
        if (str == null) {
            return EMPTY_STRING_ARRAY
        }
        if (delim == null) {
            return arrayOf(str)
        }
        return str.trim().split(delim).toTypedArray()
    }
}