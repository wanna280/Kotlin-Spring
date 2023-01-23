package com.wanna.framework.web.util

import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.AntPathMatcher

/**
 * 路径表达式
 *
 * @param pattern pattern
 * @param antMatcher 提供对于路径表达式去进行匹配的AntPathMatcher
 */
open class PathPattern(pattern: String, private val antMatcher: AntPathMatcher = AntPathMatcher()) {

    val pattern: String = cleanPath(pattern)

    open fun match(path: String): Boolean {
        return antMatcher.match(pattern, path)
    }

    /**
     * 提取给定的URL当中的模板参数
     *
     * @param path 要提取的路径
     * @return 模板参数Map<String,String>
     */
    @Nullable
    open fun extractUriTemplateVariables(path: String): Map<String, String>? {
        return antMatcher.extractUriTemplateVariables(pattern, path)
    }

    /**
     * 联合别的[PathPattern], 得到一个新的[PathPattern]
     *
     * @param other other path pattern
     * @return combined path pattern
     */
    open fun combine(other: PathPattern): PathPattern {
        return PathPattern(this.pattern + other.pattern)
    }

    /**
     * 获取干净的path, 保证path的开头一定有"/", 末尾一定不含"/", 这样路径就可以直接去进行拼接了...
     *
     * @param path path
     * @return 转换之后得到的干净的path
     */
    private fun cleanPath(path: String): String {
        var pathToUse = path
        // 如果开头不为"/", 那么补上"/"
        if (!pathToUse.startsWith("/")) {
            pathToUse = "/$pathToUse"
        }
        // 如果以"/"结尾, 那么去掉该"/"
        if (pathToUse.endsWith("/")) {
            pathToUse = pathToUse.substring(0, pathToUse.length - 1)
        }
        return pathToUse
    }

    override fun equals(@Nullable other: Any?): Boolean {
        if (this === other) return true
        if (other !is PathPattern) return false
        if (pattern != other.pattern) return false
        return true
    }

    override fun hashCode() = pattern.hashCode()

    /**
     * 获取PathPattern字符串
     *
     * @return pattern
     */
    override fun toString(): String = this.pattern
}