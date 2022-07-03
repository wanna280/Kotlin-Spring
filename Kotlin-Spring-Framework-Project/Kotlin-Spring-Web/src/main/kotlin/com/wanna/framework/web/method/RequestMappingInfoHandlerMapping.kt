package com.wanna.framework.web.method

import com.wanna.framework.web.HandlerMapping
import com.wanna.framework.web.handler.AbstractHandlerMethodMapping
import com.wanna.framework.web.server.HttpServerRequest

/**
 * 它支持了RequestMappingInfo的HandlerMapping，它实现了父类的泛型T为RequestMappingInfo；
 * 它支持去对Mapping为RequestMappingInfo的情况去进行解析和匹配
 */
abstract class RequestMappingInfoHandlerMapping : AbstractHandlerMethodMapping<RequestMappingInfo>() {

    init {
        super.setHandlerMethodMappingNamingStrategy(RequestMappingInfoHandlerMethodMappingNamingStrategy())
    }

    /**
     * 从Mapping(RequestMappingInfo)当中去获取到直接路径列表
     *
     * @param mapping(RequestMappingInfo)
     * @return Mapping当中的路径列表
     */
    override fun getDirectPaths(mapping: RequestMappingInfo): Set<String> {
        return mapping.getPaths()
    }

    /**
     * 在找到合适的Handler去处理本次请求之后，我们应该去解析PathVariables，并放入到属性当中方便后续去进行获取
     *
     * @param mapping mapping
     * @param handlerMethod HandlerMethod
     * @param request request
     */
    override fun handleMatch(mapping: RequestMappingInfo, handlerMethod: HandlerMethod, request: HttpServerRequest) {
        val url = request.getUrl()
        // 解析路径当中的模板参数(UrlTemplateVariables)
        val pattern = mapping.pathPatternsCondition.getContent().iterator().next()
        val uriTemplateVariables = pattern.extractUriTemplateVariables(url)
        request.setAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, uriTemplateVariables)
    }

    /**
     * 判断当前Mapping是否匹配当前的请求？
     *
     * @param request request
     * @param mapping mapping(RequestMappingInfo)
     * @return 如果匹配的话，return Mapping；不然return null
     */
    override fun getMatchingMapping(request: HttpServerRequest, mapping: RequestMappingInfo): RequestMappingInfo? {
        return mapping.getMatchingCondition(request)
    }
}