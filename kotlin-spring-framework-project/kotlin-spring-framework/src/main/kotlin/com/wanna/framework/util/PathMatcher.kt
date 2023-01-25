package com.wanna.framework.util

import com.wanna.framework.lang.Nullable

/**
 * 路径的匹配器, 默认实现为[AntPathMatcher]
 *
 * @see AntPathMatcher
 */
interface PathMatcher {

    /**
     * 检查给定的path是否包含了表达式
     *
     * @param path path
     * @return 如果path当中含有表达式return true; 否则return false
     */
    fun isPattern(@Nullable path: String?): Boolean

    /**
     * 执行给定的pattern和path之间的匹配, 执行的是pattern和path之间的完全匹配
     *
     * @param pattern 匹配的模式表达式
     * @param path 要匹配的路径
     */
    fun match(pattern: String, path: String): Boolean

    /**
     * 执行给定的pattern和path之间的匹配, 执行的是path和pattern的前缀之间的匹配
     *
     * @param pattern 匹配的模式表达式
     * @param path 要匹配的路径
     */
    fun matchStart(pattern: String, path: String): Boolean
}