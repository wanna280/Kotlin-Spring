package com.wanna.framework.simple.test.match

import com.wanna.framework.util.AntPathMatcher


/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/24
 */
class AntMatcherTest {
}

fun main() {
    val antPathMatcher = AntPathMatcher()
    val uriTemplates = antPathMatcher.extractUriTemplateVariables("/a/b/{name}", "/a/b/wanna")
    println(uriTemplates)
}