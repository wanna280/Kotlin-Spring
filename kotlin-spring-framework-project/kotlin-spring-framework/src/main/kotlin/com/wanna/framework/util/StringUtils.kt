package com.wanna.framework.util

import com.wanna.framework.lang.Nullable
import org.springframework.util.StringUtils

/**
 * 这是一个String的工具类
 */
object StringUtils {
    /**
     * 空的String数组
     */
    @JvmStatic
    private val EMPTY_STRING_ARRAY = emptyArray<String>()

    /**
     * 文件夹的分隔符
     */
    const val FOLDER_SEPARATOR = "/"

    /**
     * 判断一个字符串当中是否存在有文本
     *
     * @param str str
     * @return 如果有文本的话return true; 否则return false
     */
    @JvmStatic
    fun hasText(@Nullable str: String?): Boolean = !str.isNullOrBlank()

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

    /**
     * 获取干净的path
     *
     * @param path
     * @return clean path
     */
    @JvmStatic
    fun cleanPath(path: String): String {
        return StringUtils.cleanPath(path)
    }

    @JvmStatic
    fun collectionToCommaDelimitedString(@Nullable collection: Collection<String?>?): String {
        return collectionToCommaDelimitedString(collection, ",")
    }

    @JvmStatic
    fun collectionToCommaDelimitedString(@Nullable collection: Collection<String?>?, delim: String): String {
        return collectionToCommaDelimitedString(collection, delim, prefix = "", suffix = "")
    }

    @JvmStatic
    fun collectionToCommaDelimitedString(
        @Nullable collection: Collection<String?>?, delim: String, prefix: String, suffix: String
    ): String {
        if (collection.isNullOrEmpty()) {
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
    fun commaDelimitedListToStringArray(@Nullable str: String?): Array<String> {
        return commaDelimitedListToStringArray(str, ",")
    }

    /**
     * 将字符串转为字符串数组(采用delim作为分隔符)
     *
     * @param str 待进行分割的字符串
     * @param delim 分隔符
     * @return 分析完成之后的字符串列表
     */
    @JvmStatic
    fun commaDelimitedListToStringArray(@Nullable str: String?, @Nullable delim: String?): Array<String> {
        str ?: return EMPTY_STRING_ARRAY
        delim ?: return arrayOf(str)
        return str.trim().split(delim).filter { hasText(it) }.toTypedArray()
    }
}