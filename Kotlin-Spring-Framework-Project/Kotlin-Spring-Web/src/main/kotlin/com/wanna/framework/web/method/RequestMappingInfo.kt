package com.wanna.framework.web.method

import com.wanna.framework.web.bind.annotation.RequestMethod
import com.wanna.framework.web.mvc.condition.*
import com.wanna.framework.web.server.HttpServerRequest

/**
 * 它封装了@RequestMapping注解当中的各个属性的信息，负责对一个@RequestMapping去进行匹配
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
        methodsCondition.getMatchingCondition(request) ?: return null
        // 2.匹配Param
        paramsCondition.getMatchingCondition(request) ?: return null
        // 3.匹配Path
        pathPatternsCondition.getMatchingCondition(request) ?: return null
        // 4.匹配header
        headersCondition.getMatchingCondition(request) ?: return null
        // 5.匹配produces
        producesCondition.getMatchingCondition(request) ?: return null
        return this
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
        return result
    }


    /**
     * Builder，对于RequestMappingInfo的构建比较复杂，这里主要是为了更方便地去构建RequestMappingInfo
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