package com.wanna.framework.web.mvc.condition

import com.wanna.framework.web.bind.annotation.RequestMapping
import com.wanna.framework.web.server.HttpServerRequest

/**
 * 基于请求参数的Condition
 *
 * @see RequestMapping.params
 */
open class ParamsRequestCondition(private val params: List<String>) :
    AbstractRequestCondition<ParamsRequestCondition>() {
    constructor(vararg params0: String) : this(listOf(*params0))
    override fun getContent() = params
    override fun getToStringInfix() = " && "

    override fun combine(other: ParamsRequestCondition): ParamsRequestCondition {
        if (this.isEmpty() && other.isEmpty()) {
            return this
        }
        if (this.isEmpty()) {
            return other
        }
        if (other.isEmpty()) {
            return this
        }
        val params = ArrayList<String>(this.params)
        params += other.params
        return ParamsRequestCondition(this.params)
    }

    override fun getMatchingCondition(request: HttpServerRequest): ParamsRequestCondition? {
        this.params.forEach {
            if (!request.getParamNames().contains(it)) {
                return null
            }
        }
        return this
    }
}