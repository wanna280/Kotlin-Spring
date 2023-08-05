package com.wanna.framework.util

import com.wanna.framework.lang.Nullable
import java.util.regex.Pattern

/**
 * 基于Ant风格的[PathMatcher], 提供基于Ant风格的路径匹配.
 *
 * Ant表达式当中的表达式包含如下这些:
 * * 1.Ant表达式当中, "?"代表可以去匹配单个pathDir当中的任意单个字符, 例如"com/t?st.txt"这个表达式和"com/tast.txt"/"com/test.txt"都是匹配的;
 * * 2.Ant表达式当中, "*"代表可以匹配任单个pathDir当中的任意个数的字符, 例如;
 * * 3.Ant表达式当中, "**"代表可以匹配单个Path当中的多个pathDir;
 * * 4.Ant表达式当中, "{filename}"代表该位置为路径变量filename的匹配, 不仅支持单独的变量名,
 * 还支持使用"com/{filename:\\w+}.jsp"这样的表达式, 在filename之后通过":"去进行分隔了正则表达式,
 * 在进行匹配时, 会将":"之后的部分去作为正则表达式去进行匹配, 并将该部分的值传递给路径变量"filename",
 * 例如"com/test.jsp"这个路径将会匹配该表达式, 并且计算得到的路径变量filename="test".
 *
 * @see PathMatcher
 *
 * @param pathSeparator 路径的分隔符, 默认采用"/"去进行分隔
 */
open class AntPathMatcher @JvmOverloads constructor(@Nullable pathSeparator: String? = null) : PathMatcher {
    companion object {
        /**
         * 默认的路径分隔符
         */
        const val DEFAULT_PATH_SEPARATOR = "/"
    }

    /**
     * 路径的分隔符, 默认为"/"
     */
    private val pathSeparator = pathSeparator ?: DEFAULT_PATH_SEPARATOR

    /**
     * 在path拆分成为多段去进行匹配时, 是否需要将每一段的path去进行trim? 默认为不进行trim
     */
    var trimTokens = false

    /**
     * 路径匹配是否要区分大小写? 默认为true, 需要区分大小写
     */
    var caseSensitive = true

    /**
     * 检查给定的path当中, 是否含有Ant的表达式?
     * 如果path当中含有"*"/"?"/"{}"的其中一些符号, 那么就算是含有Ant表达式.
     *
     * @param path path
     * @return 如果含有"*"/"?"/"{}", 那么就return true; 否则return false
     */
    override fun isPattern(@Nullable path: String?): Boolean {
        path ?: return false
        var uriVar = false  // 记录是否遇到了'{'路径变量的前缀?
        for (index in path.indices) {
            val ch = path[index]
            // 如果遇到了"*"/"?", 那么直接return true...
            if (ch == '*' || ch == '?') {
                return true
            }

            // 如果遇到了"{", 那么将uriVar标志位设为true
            if (ch == '{') {
                uriVar = true
                continue
            }

            // 如果遇到了"}", 之前也确实是存在有"{", 那么说明存在有路径变量, return true
            if (ch == '}' && uriVar) {
                return true
            }
        }

        // 啥也没有匹配上, 正常的路径, return false
        return false
    }

    /**
     * 执行给定的pattern和path之间的匹配, 执行的是pattern和path之间的完全匹配
     *
     * @param pattern 匹配的模式表达式
     * @param path 要匹配的路径
     */
    override fun match(pattern: String, path: String): Boolean {
        return doMatch(pattern, path, true, null)
    }

    /**
     * 执行给定的pattern和path之间的匹配, 执行的是path和pattern的前缀之间的匹配
     *
     * @param pattern 匹配的模式表达式
     * @param path 要匹配的路径
     */
    override fun matchStart(pattern: String, path: String): Boolean {
        return doMatch(pattern, path, false, null)
    }

    /**
     * 从给定的path当中去提取到URL的模板参数列表
     *
     * @param pattern 待进行匹配的表达式
     * @param path 待进行提取参数的路径
     * @return 从path当中提取到的模板参数列表
     */
    open fun extractUriTemplateVariables(pattern: String, path: String): Map<String, String> {
        val uriTemplateVariables = LinkedHashMap<String, String>()

        // 执行匹配, 用于统计出来Uri TemplateVariables
        val result = doMatch(pattern, path, true, uriTemplateVariables)
        if (!result) {
            throw IllegalStateException("Pattern \"$pattern\" is not match for \"$pattern\"")
        }
        return uriTemplateVariables
    }

    /**
     * 执行真正的pattern和path之间的匹配
     *
     * @param path path
     * @param pattern pattern
     * @param fullMatch 是否进行完全匹配? 为true代表完全匹配, 为false代表用path去匹配pattern的前缀即可
     * @param uriTemplateVariables Uri模板参数列表(输出参数)
     */
    protected open fun doMatch(
        pattern: String,
        @Nullable path: String?,
        fullMatch: Boolean,
        @Nullable uriTemplateVariables: MutableMap<String, String>?
    ): Boolean {
        // 如果其中一个以"/"开头, 另外一个不是以"/"开头, 那么匹配失败
        if (path === null || pattern.startsWith(this.pathSeparator) != path.startsWith(this.pathSeparator)) {
            return false
        }

        // 按照"/"去拆分pattern
        val patternDirs = tokenizePath(pattern)

        // 按照"/"去拆分path
        val pathDirs = tokenizePath(path)

        var patternIdxStart = 0
        var patternIdxEnd = patternDirs.size - 1
        var pathIdxStart = 0
        var pathIdxEnd = pathDirs.size - 1


        // ----------第一遍匹配, 从前往后匹配...

        while (patternIdxStart <= patternIdxEnd && pathIdxStart <= pathIdxEnd) {
            val patternDir = patternDirs[patternIdxStart]

            // 如果patternDir遇到了"**", 那么break
            if (patternDir == "**") {
                break
            }
            // 匹配当前位置的patternDir和pathDir
            if (!matchStrings(patternDir, pathDirs[pathIdxStart], uriTemplateVariables)) {
                return false
            }

            patternIdxStart++
            pathIdxStart++
        }

        // 如果是因为path的全部段都匹配完了(并且和pattern的前K段完全匹配), 从而结束的循环,
        if (pathIdxStart > pathIdxEnd) {
            // 如果path的全部段用完的同时, pattern的全部段也用完了, 说明所有的段都匹配的, 但是末尾是否以"/"结尾未知, 这里去匹配一下就足够
            if (patternIdxStart > patternIdxEnd) {
                return pattern.endsWith(this.pathSeparator) == path.endsWith(this.pathSeparator)
            }
            // 如果path全部用完(exhausted), 但是pattern没用完, 但是只要求不完全匹配， 那么return true
            // 比如path="/a/b", pattern="/a/b/c", 这种情况下都算匹配, 因为只是要求匹配前缀
            if (!fullMatch) {
                return true
            }

            // 如果path全部用完, pattern还差一段的话, 那么有可能pattern末尾是一个"*"
            // 比如path="/a/b/", pattern="/a/b/*", 那么也算是完全匹配(Note: path必须后面有"/"才能算)
            if (patternIdxStart == patternIdxEnd && patternDirs[patternIdxStart] == "*" && path.endsWith(this.pathSeparator)) {
                return true
            }
            // 如果pattern还剩很多段, 或者虽然只剩下一段, 但是结束那一段不是"*"的话, 那么pattern的后面, 只能是"**", 不然都算不匹配
            // 例如path="/a/b/", pattern="/a/b/**/**"
            for (i in patternIdxStart..patternIdxEnd) {
                if (patternDirs[i] != "**") {
                    return false
                }
            }
            return true
            // 如果是因为pattern的全部段都匹配完了(并且和path的前K段完全匹配), 从而导致的循环结束, 此时path都没匹配完, 那么return false
        } else if (patternIdxStart > patternIdxEnd) {
            return false

            // 如果是因为遇到了"**"导致结束的循环, 但是并不要求完全匹配, 那么直接return true
        } else if (!fullMatch && "**" == patternDirs[patternIdxStart]) {
            return true
        }

        // Note: 当来到这里的时候, patternDirs[patternIdxStart]一定为"**"(对于别的情况, 在之前都已经返回了...)

        // ----------第二遍匹配, 从后往前匹配...
        while (pathIdxStart <= pathIdxEnd && patternIdxStart <= patternIdxEnd) {
            val patternDir = patternDirs[patternIdxEnd]
            // 如果pattern遇到了"**", 那么break
            if (patternDir == "**") {
                break
            }

            // 匹配当前位置的patternDir和pathDir
            if (!matchStrings(patternDir, pathDirs[pathIdxEnd], uriTemplateVariables)) {
                return false
            }

            // 如果当前是最后一段, 但是其中一个有"/", 另外一个没有"/", 那么肯定算不匹配, return false
            if (patternIdxEnd == patternDirs.size - 1 && pattern.endsWith(this.pathSeparator) != path.endsWith(this.pathSeparator)) {
                return false
            }

            pathIdxEnd--
            patternIdxEnd--
        }

        // 如果path的全部段都用完了, 那么只能要求pattern的全部段都是"**"了
        // 例如path="/a/b/c/d", pattern="/a/b/**/**/c/d"都算是匹配, 中间的"**"数量可以有任意个
        if (pathIdxStart > pathIdxEnd) {
            for (i in patternIdxStart..patternIdxEnd) {
                if (patternDirs[i] != "**") {
                    return false
                }
            }
            return true
        }


        // ----------第三遍匹配..

        // Note: 当来到这里的时候, patternDirs[patternIdxStart]一定为"**"(对于别的情况, 在之前都已经返回了...)

        while (patternIdxStart < patternIdxEnd && pathIdxStart <= pathIdxEnd) {
            var patternIdxTemp = -1

            // 从patternIdxStart+1的位置开始往后去进行搜索, 找到后一个"**"所在的位置...
            for (i in patternIdxStart + 1..patternIdxEnd) {
                if (patternDirs[i] == "**") {
                    patternIdxTemp = i
                    break
                }
            }

            // 如果pattern当中的后一个"**"和前一个"**"紧紧挨着, 也就是"**/**"这种情况, 那么跳过前面的一个"**"即可
            if (patternIdxTemp == patternIdxStart + 1) {
                patternIdxStart++
                continue
            }

            // patternIdxTemp的位置是下一个"**"的位置, patternIdxStart记录的是上一个"**"的位置, 例如"**/c/d/**"这种情况
            // 对于patLength记录的是就是两者之间的距离, 也就是"/c/d/"这一段的距离...
            val patLength = patternIdxTemp - patternIdxStart - 1

            // strLength用于记录一下此时path还未进行匹配的部分pathDir, 还剩下的长度有多长
            val strLength = pathIdxEnd - pathIdxStart + 1
            var foundIdx = -1

            strLoop@
            for (i in 0..strLength - patLength) {
                for (j in 0 until patLength) {
                    val subPat = patternDirs[patternIdxStart + j + 1]
                    val subStr = pathDirs[pathIdxStart + i + j]
                    if (!matchStrings(subPat, subStr, uriTemplateVariables)) {
                        continue@strLoop
                    }
                }
                foundIdx = pathIdxStart + i
                break
            }

            if (foundIdx == -1) {
                return false
            }

            // 跳转到去匹配下一个表达式"**"的地方, 例如pattern="/a/**/**/c/**/**/e", path="/a/b/b/c/d/d/e"

            // 对于patternIdxStart, 直接跳转到patternIdxTemp的地方去即可, 这个地方正好就是下一个"**"的位置...
            patternIdxStart = patternIdxTemp

            // 对于path, 也去往前去进行跳转
            pathIdxStart = foundIdx + patLength
        }

        for (i in patternIdxStart..patternIdxEnd) {
            if (patternDirs[i] != "**") {
                return false
            }
        }
        return true
    }

    /**
     * 对单个字符串的pattern和字符串去进行匹配
     *
     * @param pattern pattern
     * @param str str
     * @param uriTemplateVariables uri模板参数列表
     */
    private fun matchStrings(
        pattern: String,
        str: String,
        @Nullable uriTemplateVariables: MutableMap<String, String>?
    ): Boolean {
        return AntPathStringMatcher(pattern, this.caseSensitive).matchStrings(str, uriTemplateVariables)
    }

    /**
     * 将给定的path, 按照'/'去进行拆分得到数组
     *
     * @param path path
     * @return 拆分之后的path
     */
    protected open fun tokenizePath(path: String): Array<String> {
        return StringUtils.tokenizeToStringArray(path, this.pathSeparator, this.trimTokens, true)
    }


    /**
     * Ant风格的字符串Matcher, 将pattern字符串转换成为正则表达式的方式去进行匹配
     *
     * @param pattern 原始的Ant风格的表达式
     * @param caseSensitive 是否大小写敏感? 如果为true的话, 需要区分大小写的匹配; 如果为false, 则进行不区分大小写的匹配
     */
    protected open class AntPathStringMatcher(pattern: String, val caseSensitive: Boolean) {

        companion object {
            /**
             * 用于去检查原始的Ant风格的表达式当中是否存在有通配符的正则表达式
             */
            @JvmStatic
            private val GLOB_PATTERN = Pattern.compile("\\?|\\*|\\{((?:\\{[^/]+?\\}|[^/{}]|\\\\[{}])+?)\\}")

            /**
             * 默认的变量的表达式
             */
            @JvmStatic
            private val DEFAULT_VARIABLE_PATTERN = "((?s).*)"
        }

        /**
         * 原始的Ant风格的表达式字符串
         */
        private val rawPattern: String = pattern

        /**
         * 执行对于路径字符串的匹配的正则表达式, 从rawPattern的Ant风格转换而来,
         * Note: 如果exactMatch=true, 那么pattern=null
         */
        private val pattern: Pattern?

        /**
         * 是否进行精准匹配? 对于pattern当中不含有表达式的情况下, 可以直接使用字符串的精准匹配
         */
        private val exactMatch: Boolean

        /**
         * 在Ant表达式当中的路径变量名的列表
         */
        private val variableNames = ArrayList<String>()

        init {
            var end = 0
            val patternBuilder = StringBuilder()
            val matcher = GLOB_PATTERN.matcher(pattern)

            while (matcher.find()) {
                patternBuilder.append(quote(pattern, end, matcher.start()))
                val match = matcher.group()

                // 将Ant表达式当中的"?"转为"."(任意单个字符), 将"*"转换成为".*"(任意个数的字符)
                if (match == "?") {
                    patternBuilder.append(".")
                } else if (match == "*") {
                    patternBuilder.append(".*")

                    // 如果pattern当中存在有"{name}"这样的路径变量...
                } else if (match.startsWith("{") && match.endsWith("}")) {
                    val colonIndex = match.indexOf(':')

                    // 如果不含有":", 那么"{"和"}"中间就是变量名, 变量表达式使用默认的正则表达式
                    if (colonIndex == -1) {
                        patternBuilder.append(DEFAULT_VARIABLE_PATTERN)
                        this.variableNames += match.substring(1, match.length - 1)

                        // 如果含有":"的话, 那么":"前面的部分才是变量名, ":"后面的部分是变量的正则表达式
                    } else {
                        val variablePattern = match.substring(colonIndex + 1, match.length - 1)
                        patternBuilder.append('(').append(variablePattern).append(')')

                        // 计算变量名...
                        this.variableNames += match.substring(1, colonIndex)
                    }
                }
                end = matcher.end()
            }

            // 如果没有找到grob表达式, 说明给定的Ant表达式的Pattern不含有表达式, 只是单纯的字符串, 可以走字符串的匹配
            if (end == 0) {
                exactMatch = true
                this.pattern = null
            } else {
                exactMatch = false
                patternBuilder.append(quote(pattern, end, pattern.length))
                val flags = Pattern.DOTALL or (if (this.caseSensitive) 0 else Pattern.CASE_INSENSITIVE)
                this.pattern = Pattern.compile(patternBuilder.toString(), flags)
            }
        }


        /**
         * 执行对于给定的字符串的匹配
         *
         * @param str 待进行匹配的路径字符串
         * @param uriTemplateVariables 从path当中提取到的Uri模板参数列表(如果匹配成功, 将会把路径参数放入这个Map当中来)
         * @return 执行匹配是否成功?
         */
        fun matchStrings(str: String, @Nullable uriTemplateVariables: MutableMap<String, String>?): Boolean {
            if (this.exactMatch) {
                return this.rawPattern.equals(str, !caseSensitive)
            } else if (this.pattern != null) {
                val matcher = this.pattern.matcher(str)
                if (matcher.matches()) {
                    if (uriTemplateVariables != null) {
                        // 如果路径变量的数量不匹配的话...
                        if (variableNames.size != matcher.groupCount()) {
                            throw IllegalArgumentException(
                                "The number of capturing groups in the pattern segment $pattern does not match the number of URI template variables it defines, " +
                                        "which can occur if capturing groups are used in a URI template regex. Use non-capturing groups instead."
                            )
                        }

                        for (i in 1..matcher.groupCount()) {
                            val name = this.variableNames[i - 1]
                            // 对于路径变量, 不支持使用"*"
                            if (name.startsWith("*")) {
                                throw IllegalArgumentException("Capturing patterns ($name) are not supported by the AntPathMatcher. Use the PathPatternParser instead.")
                            }
                            val value = matcher.group(i)
                            uriTemplateVariables[name] = value
                        }
                    }

                    return true
                }
            }
            return false
        }

        private fun quote(s: String, start: Int, end: Int): String {
            return if (start == end) "" else Pattern.quote(s.substring(start, end))
        }
    }

}