package com.wanna.framework.util

import org.springframework.util.AntPathMatcher

/**
 * AntPathMatcher，这里我们直接使用Spring提供的工具类即可
 *
 * @see AntPathMatcher
 */
open class AntPathMatcher : PathMatcher {
    companion object {
        const val DEFAULT_PATH_SEPARATOR = AntPathMatcher.DEFAULT_PATH_SEPARATOR
    }

    private val antMatcher = AntPathMatcher()
    override fun match(pattern: String, path: String): Boolean {
        return antMatcher.match(pattern, path)
    }

    /**
     * 提取URL的模板参数
     */
    fun extractUriTemplateVariables(pattern: String, path: String): Map<String, String>? {
        return antMatcher.extractUriTemplateVariables(pattern, path)
    }
}