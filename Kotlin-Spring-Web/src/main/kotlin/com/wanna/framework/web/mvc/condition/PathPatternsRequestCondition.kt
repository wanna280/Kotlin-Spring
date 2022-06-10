package com.wanna.framework.web.mvc.condition

import com.wanna.framework.util.AntPathMatcher
import com.wanna.framework.util.PathMatcher
import com.wanna.framework.web.server.HttpServerRequest

/**
 * 基于路径的模式匹配
 */
open class PathPatternsRequestCondition(private val paths: Set<String>) :
    AbstractRequestCondition<PathPatternsRequestCondition>() {
    constructor(vararg paths: String) : this(setOf(*paths))

    private val matcher: PathMatcher = AntPathMatcher()

    override fun getContent() = paths
    override fun getToStringInfix() = " && "

    override fun combine(other: PathPatternsRequestCondition): PathPatternsRequestCondition {
        if (other.isEmpty()) {
            return this
        }
        val paths = HashSet<String>()
        other.paths.forEach { o -> this.paths.forEach { paths += "$o$it" } }
        return PathPatternsRequestCondition(paths)
    }

    /**
     * 获取匹配的结果，我们使用AntMatcher去进行路径的匹配；
     *
     * @return 如果给定的request的路径合法的话，return this；不合法的话，return null
     */
    override fun getMatchingCondition(request: HttpServerRequest): PathPatternsRequestCondition? {
        val url = request.getUrl()
        paths.forEach {
            if (matcher.match(it, url)) {
                return this
            }
        }
        return null
    }

}