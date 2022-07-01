package com.wanna.framework.web.util

import org.springframework.util.AntPathMatcher

class PathPattern(val pattern: String, private val antMatcher: AntPathMatcher = AntPathMatcher()) {
    fun match(path: String): Boolean {
        return antMatcher.match(pattern, path)
    }

    /**
     * 提取URL的模板参数
     *
     * @param path 要提取的路径
     * @return 模板参数Map<String,String>
     */
    fun extractUriTemplateVariables(path: String): Map<String, String>? {
        return antMatcher.extractUriTemplateVariables(pattern, path)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PathPattern) return false
        if (pattern != other.pattern) return false
        return true
    }

    override fun hashCode() = pattern.hashCode()
}