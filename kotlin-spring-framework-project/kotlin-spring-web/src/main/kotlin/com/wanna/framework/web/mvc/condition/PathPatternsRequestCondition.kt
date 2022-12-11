package com.wanna.framework.web.mvc.condition

import com.wanna.framework.web.bind.annotation.RequestMapping
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.util.PathPattern

/**
 * 基于路径的模式匹配
 *
 * @see RequestMapping.path
 */
open class PathPatternsRequestCondition(private val patterns: Set<PathPattern>) :
    AbstractRequestCondition<PathPatternsRequestCondition>() {
    constructor(vararg paths: String) : this(LinkedHashSet(paths.map { PathPattern(it) }.toList()))

    val paths = patterns.map { it.pattern }.toList()
    override fun getContent() = patterns
    override fun getToStringInfix() = " && "

    override fun combine(other: PathPatternsRequestCondition): PathPatternsRequestCondition {
        if (other.isEmpty()) {
            return this
        }
        val paths = HashSet<String>()
        other.paths.forEach { o -> this.paths.forEach { paths += "$o$it" } }

        // bugfix: 如果方法上的@RequestMapping上没有配置路径的话, 那么我们需要沿用类上的@RequestMapping的路径
        // 这里this为方法上的Mapping, other类上是Mapping
        if (this.paths.isEmpty()) {
            paths += other.paths
        }
        return PathPatternsRequestCondition(*paths.toTypedArray())
    }

    /**
     * 获取匹配的结果，我们使用AntMatcher去进行路径的匹配；
     *
     * @return 如果给定的request的路径合法的话，return this；不合法的话，return null
     */
    override fun getMatchingCondition(request: HttpServerRequest): PathPatternsRequestCondition? {
        val url = request.getUri()
        patterns.forEach {
            if (it.match(url)) {
                val paths = LinkedHashSet<PathPattern>()
                paths += it
                paths += paths
                return this
            }
        }
        return null
    }

}