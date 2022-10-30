package com.wanna.framework.web.method

import com.wanna.framework.web.bind.annotation.RequestMethod
import com.wanna.framework.web.mvc.condition.*
import com.wanna.framework.web.server.HttpServerRequest

/**
 * 它封装了@RequestMapping注解当中的各个属性的信息，负责对一个@RequestMapping去进行匹配
 *
 * @param methodsCondition 对应@RequestMapping当中的method属性(只有拥有这些methods的情况下，当前RequestMapping才能被匹配到)
 * @param pathPatternsCondition 对应@RequestMapping当中的path属性(只有拥有这些path的情况下，当前RequestMapping才能被匹配到)
 * @param paramsCondition 对应@RequestMapping当中的params属性(只有拥有这些params的情况下，当前RequestMapping才能被匹配到)
 * @param headersCondition 对应@RequestMapping当中的headers属性(只有拥有这些headers的情况下，当前RequestMapping才能被匹配到)
 * @param producesCondition 对应@RequestMapping当中的produces属性，设置当前请求的产出类型(要以什么类型的数据去进行写出给客户端)
 *
 * @see com.wanna.framework.web.bind.annotation.RequestMapping
 */
open class RequestMappingInfo(
    val methodsCondition: RequestMethodsRequestCondition = RequestMethodsRequestCondition(),
    val pathPatternsCondition: PathPatternsRequestCondition = PathPatternsRequestCondition(),
    val paramsCondition: ParamsRequestCondition = ParamsRequestCondition(),
    val headersCondition: HeadersRequestCondition = HeadersRequestCondition(),
    val producesCondition: ProducesRequestCondition = ProducesRequestCondition()
) {
    open fun getPaths(): Set<String> = HashSet(pathPatternsCondition.paths)

    /**
     * 对请求去进行匹配，看当前的Mapping是否支持处理该request
     *
     * @param request request
     */
    open fun getMatchingCondition(request: HttpServerRequest): RequestMappingInfo? {
        // 1.匹配请求方式
        val methodsCondition = methodsCondition.getMatchingCondition(request) ?: return null
        // 2.匹配Param
        val paramsCondition = paramsCondition.getMatchingCondition(request) ?: return null
        // 3.匹配Path
        val pathCondition = pathPatternsCondition.getMatchingCondition(request) ?: return null
        // 4.匹配header
        val headersCondition = headersCondition.getMatchingCondition(request) ?: return null
        // 5.匹配produces
        val producesCondition = producesCondition.getMatchingCondition(request) ?: return null

        // build RequestMappingInfo for Match
        return RequestMappingInfo(
            methodsCondition,
            pathCondition,
            paramsCondition,
            headersCondition,
            producesCondition
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RequestMappingInfo) return false
        if (methodsCondition != other.methodsCondition) return false
        if (pathPatternsCondition != other.pathPatternsCondition) return false
        if (paramsCondition != other.paramsCondition) return false
        if (headersCondition != other.headersCondition) return false
        return true
    }

    override fun hashCode(): Int {
        var result = methodsCondition.hashCode()
        result = 31 * result + pathPatternsCondition.hashCode()
        result = 31 * result + paramsCondition.hashCode()
        result = 31 * result + headersCondition.hashCode()
        result = 31 * result + producesCondition.hashCode()  // fixed
        return result
    }

    override fun toString(): String =
        "methods=$methodsCondition, " +
                "pathPatterns=$pathPatternsCondition, " +
                "params=$paramsCondition, " +
                "headers=$headersCondition, " +
                "produces=$producesCondition"


    /**
     * RequestMappingInfo的Builder，对于RequestMappingInfo的构建比较复杂，这里主要是为了更方便地去构建RequestMappingInfo
     *
     * @see com.wanna.framework.web.bind.annotation.RequestMapping
     */
    class Builder {
        private var methodsCondition: RequestMethodsRequestCondition = RequestMethodsRequestCondition()
        private var pathPatternsCondition: PathPatternsRequestCondition = PathPatternsRequestCondition()
        private var paramsCondition: ParamsRequestCondition = ParamsRequestCondition()
        private var headersCondition: HeadersRequestCondition = HeadersRequestCondition()
        private var producesCondition: ProducesRequestCondition = ProducesRequestCondition()
        fun paths(vararg paths: String): Builder {
            this.pathPatternsCondition = PathPatternsRequestCondition(*paths)
            return this
        }

        fun paths(pathPatternsCondition: PathPatternsRequestCondition): Builder {
            this.pathPatternsCondition = pathPatternsCondition
            return this
        }

        fun params(vararg params: String): Builder {
            this.paramsCondition = ParamsRequestCondition(*params)
            return this
        }

        fun params(paramsRequestCondition: ParamsRequestCondition): Builder {
            this.paramsCondition = paramsRequestCondition
            return this
        }

        fun methods(vararg methods: RequestMethod): Builder {
            this.methodsCondition = RequestMethodsRequestCondition(*methods)
            return this
        }

        fun methods(methodsCondition: RequestMethodsRequestCondition): Builder {
            this.methodsCondition = methodsCondition
            return this
        }

        fun headers(vararg headers: String): Builder {
            this.headersCondition = HeadersRequestCondition(*headers)
            return this
        }

        fun headers(headersCondition: HeadersRequestCondition): Builder {
            this.headersCondition = headersCondition
            return this
        }

        fun produces(vararg produces: String): Builder {
            this.producesCondition = ProducesRequestCondition(*produces)
            return this
        }

        fun produces(producesCondition: ProducesRequestCondition): Builder {
            this.producesCondition = producesCondition
            return this
        }

        fun build(): RequestMappingInfo =
            RequestMappingInfo(
                methodsCondition,
                pathPatternsCondition,
                paramsCondition,
                headersCondition,
                producesCondition
            )
    }
}