package com.wanna.framework.web.method

import com.wanna.framework.web.bind.RequestMethod
import com.wanna.framework.web.mvc.condition.HeadersRequestCondition
import com.wanna.framework.web.mvc.condition.ParamsRequestCondition
import com.wanna.framework.web.mvc.condition.PathPatternsRequestCondition
import com.wanna.framework.web.mvc.condition.RequestMethodsRequestCondition
import com.wanna.framework.web.server.HttpServerRequest

/**
 * 它封装了@RequestMapping注解当中的各个属性的信息，负责对一个@RequestMapping去进行匹配
 */
open class RequestMappingInfo(
    val methodsCondition: RequestMethodsRequestCondition = RequestMethodsRequestCondition(),
    val pathPatternsCondition: PathPatternsRequestCondition = PathPatternsRequestCondition(),
    val paramsCondition: ParamsRequestCondition = ParamsRequestCondition(),
    val headersCondition: HeadersRequestCondition = HeadersRequestCondition()
) {
    open fun getPaths(): Set<String> = HashSet(pathPatternsCondition.getContent())

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
        return this
    }

    /**
     * Builder，方便更方便地去构建RequestMappingInfo
     */
    class Builder {
        private var methodsCondition: RequestMethodsRequestCondition = RequestMethodsRequestCondition()
        private var pathPatternsCondition: PathPatternsRequestCondition = PathPatternsRequestCondition()
        private var paramsCondition: ParamsRequestCondition = ParamsRequestCondition()
        private var headersCondition: HeadersRequestCondition = HeadersRequestCondition()
        fun paths(vararg paths: String): Builder {
            this.pathPatternsCondition = PathPatternsRequestCondition(*paths)
            return this
        }

        fun params(vararg params: String): Builder {
            this.paramsCondition = ParamsRequestCondition(*params)
            return this
        }

        fun methods(vararg methods: RequestMethod): Builder {
            this.methodsCondition = RequestMethodsRequestCondition(*methods)
            return this
        }

        fun headers(vararg headers: String): Builder {
            this.headersCondition = HeadersRequestCondition(*headers)
            return this
        }

        fun build(): RequestMappingInfo =
            RequestMappingInfo(methodsCondition, pathPatternsCondition, paramsCondition, headersCondition)
    }
}