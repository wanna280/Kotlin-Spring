package com.wanna.framework.util

/**
 * 路径的匹配器, 默认实现为AntMatcher
 */
interface PathMatcher {

    /**
     * 检查给定的location是否包含了表达式
     *
     * @param location location
     * @return 如果location当中含有表达式return true; 否则return false
     */
    fun isPattern(location: String): Boolean

    /**
     * 匹配指定的url路径是否合法
     *
     * @param pattern 匹配的模式表达式
     * @param path 要匹配的路径
     */
    fun match(pattern: String, path: String): Boolean
}