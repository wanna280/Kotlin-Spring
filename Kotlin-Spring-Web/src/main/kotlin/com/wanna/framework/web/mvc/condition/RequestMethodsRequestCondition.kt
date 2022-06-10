package com.wanna.framework.web.mvc.condition

import com.wanna.framework.web.bind.RequestMethod
import com.wanna.framework.web.server.HttpServerRequest

/**
 * 基于请求方式的RequestCondition的实现
 *
 * @param methods 支持的请求方式列表
 */
open class RequestMethodsRequestCondition(private val methods: Set<RequestMethod>) :
    AbstractRequestCondition<RequestMethodsRequestCondition>() {
    constructor(vararg requestMethods: RequestMethod) : this(methods = HashSet<RequestMethod>(listOf(*requestMethods)))

    override fun getContent() = methods
    override fun getToStringInfix() = " || "

    override fun combine(other: RequestMethodsRequestCondition): RequestMethodsRequestCondition {
        if (this.isEmpty() && other.isEmpty()) {
            return this
        } else if (this.isEmpty()) {
            return other
        } else if (other.isEmpty()) {
            return this
        }
        val methods = HashSet<RequestMethod>(this.methods)
        methods += other.methods
        return RequestMethodsRequestCondition(methods)
    }

    override fun getMatchingCondition(request: HttpServerRequest): RequestMethodsRequestCondition? {
        if (methods.isEmpty()) {
            return this
        }
        return if (this.methods.contains(request.getMethod())) this else null
    }
}