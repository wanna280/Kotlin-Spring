package com.wanna.framework.util

import org.springframework.util.AntPathMatcher

/**
 * AntPathMatcher，这里我们直接使用Spring提供的工具类即可
 *
 * @see AntPathMatcher
 */
open class AntPathMatcher : PathMatcher {
    private val antMatcher = AntPathMatcher()
    override fun match(pattern: String, path: String): Boolean {
        return antMatcher.match(pattern, path)
    }
}