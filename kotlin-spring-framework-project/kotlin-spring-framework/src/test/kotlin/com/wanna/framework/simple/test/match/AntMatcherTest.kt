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

private val antPathMatcher = AntPathMatcher()
private val springAntMatcher = org.springframework.util.AntPathMatcher()

fun main() {
    assert(
        antPathMatcher.extractUriTemplateVariables("/a/b/{name}", "/a/b/wanna")
                ==
                springAntMatcher.extractUriTemplateVariables("/a/b/{name}", "/a/b/wanna")
    )

    assert(
        antPathMatcher.match("/a/b/*", "/a/b/wanna")
                ==
                springAntMatcher.match("/a/b/*", "/a/b/wanna")
    )

    assert(
        antPathMatcher.match("/a/b/**", "/a/b/wanna")
                ==
                springAntMatcher.match("/a/b/**", "/a/b/wanna")
    )

    assert(
        antPathMatcher.match("/a/b/**", "/a/b")
                ==
                springAntMatcher.match("/a/b/**", "/a/b")
    )

    assert(
        antPathMatcher.match("/a/b/**", "/a/b/")
                ==
                springAntMatcher.match("/a/b/**", "/a/b/")
    )

    assert(
        antPathMatcher.match("/a/b/*/d", "/a/b/c/d")
                ==
                springAntMatcher.match("/a/b/*/d", "/a/b/c/d")
    )

    assert(
        antPathMatcher.match("/a/b/**/**/d", "/a/b/c/d")
                ==
                springAntMatcher.match("/a/b/**/**/d", "/a/b/c/d")
    )

    assert(
        antPathMatcher.match("/a/**/**/c/**/**/e", "/a/b/b/c/d/d/e")
                ==
                springAntMatcher.match("/a/**/**/c/**/**/e", "/a/b/b/c/d/d/e")
    )
}