package com.wanna.framework.util

import com.wanna.framework.lang.Nullable
import java.beans.Introspector

/**
 * 这是一个String的工具类
 */
object StringUtils {

    /**
     * Windows的文件夹分隔符
     */
    private const val WINDOWS_FOLDER_SEPARATOR = "\\"

    /**
     * 文件夹的分隔符
     */
    const val FOLDER_SEPARATOR = "/"

    /**
     * 当前目录的path为"."
     */
    private const val CURRENT_PATH = "."

    /**
     * 上一级目录的path为".."
     */
    private const val TOP_PATH = ".."

    /**
     * 文件扩展名的分隔符为"."
     */
    private const val EXTENSION_SEPARATOR = '.'

    /**
     * 空的String数组
     */
    @JvmStatic
    private val EMPTY_STRING_ARRAY = emptyArray<String>()

    /**
     * 判断一个字符串当中是否存在有文本
     *
     * @param str str
     * @return 如果有文本的话return true; 否则return false
     */
    @JvmStatic
    fun hasText(@Nullable str: String?): Boolean = !str.isNullOrBlank()

    /**
     * 判断给定的字符串是否有长度?
     *
     * @param str str
     * @return 如果为null/"", return false; 否则return true
     */
    @JvmStatic
    fun hasLength(@Nullable str: String?): Boolean = !str.isNullOrEmpty()

    /**
     * 检查给定的字符串是否为空?
     *
     * @param str str
     * @return 如果为null/"", return false; 否则return true
     */
    @JvmStatic
    fun isEmpty(@Nullable str: String?): Boolean = hasLength(str)

    /**
     * 判断str在startIndex位置之后的部分字符是否和substring匹配
     *
     * @param str 要匹配的源(src)字符串
     * @param startIndex 匹配的字符串的开始位置, 也就是说明匹配的字符串为str[startIndex]之后的部分的字符串
     * @param substring 匹配的目标(target)字符串
     */
    @JvmStatic
    fun substringMatch(str: CharSequence, startIndex: Int, substring: CharSequence): Boolean {
        // 如果str当中字符数量已经不够了, return
        if (startIndex + substring.length > str.length) {
            return false
        }
        // 匹配, str在startIndex之后的元素和substring去进行匹配
        for (i in substring.indices) {
            if (str[i + startIndex] != substring[i]) {
                return false
            }
        }
        return true
    }

    /**
     * 将给定的字符串的第一个字母变成大写, 使用[String.uppercase]去转换第一个字符, 别的字符不发生变化
     *
     * @param str 待转换的字符串
     * @return 转换之后的字符串
     */
    @JvmStatic
    fun capitalize(str: String): String = changeFirstCharacterCase(str, true)

    /**
     * 将给定的字符串的第一个字母变成小写, 使用[String.lowercase]去转换第一个字符, 别的字符不发生变化
     *
     * @param str 待转换的字符串
     * @return 转换之后的字符串
     */
    @JvmStatic
    fun uncapitalize(str: String): String = changeFirstCharacterCase(str, false)

    /**
     * 将给定的字符串的第一个字母变成小写, 使用[String.lowercase]去转换第一个字符, 别的字符不发生变化
     *
     * Note: 特殊地, 如果第一个和第二个字母都是大写, 那么就不进行转换了
     *
     * @param str 待转换的字符串
     * @return 转换之后的字符串
     * @see Introspector.decapitalize
     */
    @JvmStatic
    fun uncapitalizeAsProperty(str: String): String {
        if (!hasText(str) || (str.length > 1 && str[0].isUpperCase() && str[1].isUpperCase())) {
            return str
        }
        return changeFirstCharacterCase(str, false)
    }

    /**
     * 改变给定的字符串的第一个字母的大小写
     *
     * @param str 待转换的字符串
     * @param capitalize 为true代表转为大写, 为false代表转为小写
     * @return 转换之后的字符串
     */
    @JvmStatic
    private fun changeFirstCharacterCase(str: String, capitalize: Boolean): String {
        if (str.isBlank()) {
            return str
        }
        val baseChar = str[0]
        val updatedChar = if (capitalize) {
            baseChar.uppercaseChar()
        } else {
            baseChar.lowercaseChar()
        }
        if (baseChar == updatedChar) {
            return str
        }
        val chars = str.toCharArray()
        chars[0] = updatedChar
        return String(chars)
    }

    /**
     * 获取干净的path
     *
     * @param path path
     * @return clean path
     */
    @JvmStatic
    fun cleanPath(path: String): String {
        if (!hasText(path)) {
            return path
        }

        // 将"\"去替换成为"/"
        val normalizedPath = replace(path, WINDOWS_FOLDER_SEPARATOR, FOLDER_SEPARATOR)
        var pathToUse = normalizedPath

        // 快速检查, 如果不存在有"."的话, 那么直接return...
        if (pathToUse.indexOf('.') == -1) {
            return pathToUse
        }

        val prefixIndex = pathToUse.indexOf(":")
        var prefix = ""

        // eg: path="file:core/../core/io/Resource.class"
        // 切取prefix得到prefix="file:", pathToUse="core/../core/io/Resource.class"
        if (prefixIndex != -1) {
            prefix = pathToUse.substring(0, prefixIndex + 1)
            if (prefix.contains(FOLDER_SEPARATOR)) {
                prefix = ""
            } else {
                pathToUse = pathToUse.substring(prefixIndex + 1)
            }
        }

        // 如果path是以"/"作为开头的, 那么把这部分算到prefix当中, 并从pathToUse当中去移除掉...
        if (pathToUse.startsWith(FOLDER_SEPARATOR)) {
            prefix += FOLDER_SEPARATOR
            pathToUse = pathToUse.substring(1)
        }


        // 将pathToUse按照"/"去切割成为数组...
        val pathArray = commaDelimitedListToStringArray(pathToUse, FOLDER_SEPARATOR)

        // path当中的元素列表, 最多也就有pathArray的size, 不会更多了, 因此明确给定size...
        val pathElements = ArrayDeque<String>(pathArray.size)
        var tops = 0

        // 检查是否有".."和".", 这里需要统计".."出现的次数, 最终我们需要去将".."去放到路径的最前面去...
        for (p in pathArray) {
            if (p == TOP_PATH) {
                tops++
            } else if (p == CURRENT_PATH) {
                // skip "."
            } else {
                // 如果遇到了一层非".."的情况, 那么".."的层数去掉一层...相当于回上一级&去下一级, 两者相互抵消
                if (tops > 0) {
                    tops--
                } else {
                    pathElements.add(p)
                }
            }
        }

        // 如果path当中没有".", 也没有"..", 那么直接return
        if (pathArray.size == pathElements.size) {
            return pathToUse
        }

        // 将给定的".."全部添加到最前面去
        for (i in 0 until tops) {
            pathElements.addFirst(TOP_PATH)
        }

        // 如果pathElements当中只要一个空元素的话...那么最起码也得去添加一个"."
        if (pathElements.size == 1 && pathElements[0].isEmpty() && !prefix.startsWith(FOLDER_SEPARATOR)) {
            pathElements.addFirst(CURRENT_PATH)
        }

        // 使用"/"去连接各级别的路径...
        val joined = collectionToCommaDelimitedString(pathElements, FOLDER_SEPARATOR)

        // 将prefix拼接到计算得到的joined path之前
        return if (prefix.isEmpty()) joined else prefix + joined
    }

    /**
     * 将字符串当中的[oldPattern]去替换成为[newPattern]
     *
     * @param inString 待替换的字符串
     * @param oldPattern 旧的表达式
     * @param newPattern 新的表达式
     * @return 替换之后得到的新字符串
     */
    @JvmStatic
    fun replace(inString: String, oldPattern: String, @Nullable newPattern: String?): String {
        if (!hasLength(inString) || !hasLength(oldPattern) || newPattern == null) {
            return inString
        }
        var index = inString.indexOf(oldPattern)
        if (index == -1) {
            return inString
        }

        var capacity = inString.length
        if (newPattern.length > oldPattern.length) {
            capacity += 16
        }
        val sb = StringBuilder(capacity)
        // 当遇到oldPattern时, 将它替换成为newPattern...
        var pos = 0
        val patLen = oldPattern.length
        while (index >= 0) {
            sb.append(inString, pos, index)
            sb.append(newPattern)
            pos = index + patLen

            // 重新计算index
            index = inString.indexOf(oldPattern, pos)
        }

        // 把剩下的字符串全部append到最后
        sb.append(inString, pos, inString.length)
        return sb.toString()
    }

    /**
     * 从给定的路径当中, 去提取到文件扩展名
     *
     * @param path 文件路径
     * @return 文件扩展名(如果不存在扩展名, return null)
     */
    @Nullable
    @JvmStatic
    fun getFilenameExtension(@Nullable path: String?): String? {
        path ?: return null
        val extensionIndex = path.lastIndexOf(EXTENSION_SEPARATOR)
        if (extensionIndex == -1) {
            return null
        }
        val folderIndex = path.lastIndexOf(FOLDER_SEPARATOR)
        if (folderIndex > extensionIndex) {
            return null
        }
        return path.substring(extensionIndex + 1)
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