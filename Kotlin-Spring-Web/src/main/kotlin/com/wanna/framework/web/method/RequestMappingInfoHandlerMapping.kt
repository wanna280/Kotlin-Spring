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
     * 从Mapping当中去获取到直接路径列表
     *
     * @param mapping
     */
    override fun getDirectPaths(mapping: RequestMappingInfo): Set<String> {
        return mapping.paths.toSet()
    }

    /**
     * 判断当前Mapping是否匹配当前的请求？
     */
    override fun getMatchingMapping(request: HttpServerRequest, mapping: RequestMappingInfo): RequestMappingInfo? {
        return mapping
    }
}