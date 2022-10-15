package com.wanna.framework.web.mvc.condition

import com.wanna.framework.web.server.HttpServerRequest

/**
 * 基于请求头的Condition
 */
open class HeadersRequestCondition(val headers: Set<String>) : AbstractRequestCondition<HeadersRequestCondition>() {
    constructor(vararg headers0: String) : this(setOf(*headers0))

    override fun getContent() = this.headers

    override fun getToStringInfix() = " && "

    override fun combine(other: HeadersRequestCondition): HeadersRequestCondition {
        if (this.isEmpty() && other.isEmpty()) {
            return this
        }
        if (this.isEmpty()) {
            return other
        }
        if (other.isEmpty()) {
            return this
        }
        val headers = HashSet<String>(this.headers)
        headers += other.headers
        return HeadersRequestCondition(headers)
    }

    override fun getMatchingCondition(request: HttpServerRequest): HeadersRequestCondition? {
        headers.forEach {
            if (!request.getHeaderNames().contains(it)) {
                return null
            }
        }
        return this
    }
}