package com.wanna.framework.web.method

import com.wanna.framework.web.handler.AbstractHandlerMethodMapping
import com.wanna.framework.web.server.HttpServerRequest

/**
 * 它支持了RequestMappingInfo的HandlerMapping，它实现了父类的泛型T为RequestMappingInfo；
 * 它支持去对Mapping为RequestMappingInfo进行解析和匹配
 */
abstract class RequestMappingInfoHandlerMapping : AbstractHandlerMethodMapping<RequestMappingInfo>() {

    init {
        super.setHandlerMethodMappingNamingStrategy(RequestMappingInfoHandlerMethodMappingNamingStrategy())
    }

    /**
     * 从Mapping(RequestMappingInfo)当中去获取到直接路径列表
     *
     * @param mapping
     * @return Mapping当中的路径列表
     */
    override fun getDirectPaths(mapping: RequestMappingInfo): Set<String> {
        return mapping.paths.toSet()
    }

    /**
     * 判断当前Mapping是否匹配当前的请求？目前我们提供的是，基于方法的匹配规则
     *
     * @param request request
     * @param mapping mapping(RequestMappingInfo)
     * @return 如果匹配的话，return Mapping；不然return null
     */
    override fun getMatchingMapping(request: HttpServerRequest, mapping: RequestMappingInfo): RequestMappingInfo? {
        return if (mapping.methods.isEmpty() || mapping.methods.contains(request.getMethod())) mapping else null
    }
}