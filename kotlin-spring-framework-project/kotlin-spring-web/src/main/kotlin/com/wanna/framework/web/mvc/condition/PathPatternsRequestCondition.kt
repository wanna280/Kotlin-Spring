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

    /**
     * 联合别的[PathPatternsRequestCondition], 将该path去添加到当前的[PathPatternsRequestCondition]之后,
     * 通常情况下this为类上的path, other为方法上的path
     *
     * @param other other path patterns
     * @return combined path patterns condition
     */
    override fun combine(other: PathPatternsRequestCondition): PathPatternsRequestCondition {
        // 如果other为空, 那么直接return this即可
        if (other.isEmpty()) {
            return this
        }
        val patterns = LinkedHashSet<PathPattern>()

        // 如果其中一个的path为空, 那么返回另外一个的path...
        if (this.paths.isEmpty()) {
            patterns += other.patterns
        } else if (other.paths.isEmpty()) {
            patterns += this.patterns

            // 如果两者都不为空, 那么叉乘去生成combine的path...
        } else {
            for (pattern in this.patterns) {
                for (otherPattern in other.patterns) {
                    patterns += pattern.combine(otherPattern)
                }
            }
        }
        return PathPatternsRequestCondition(patterns)
    }

    /**
     * 获取匹配的结果, 我们使用AntMatcher去进行路径的匹配;
     *
     * @return 如果给定的request的路径合法的话, return this; 不合法的话, return null
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