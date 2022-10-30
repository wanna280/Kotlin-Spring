package com.wanna.boot.actuate.web.mappings.mvc

/**
 * 对于DispatcherHandler当中的一个Mapping的描述信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/31
 */
class DispatcherHandlerMappingDetails {

    /**
     * HandlerMethod的描述信息
     */
    var handlerMethod: HandlerMethodDescription? = null

    /**
     * 对于RequestMappingInfo的描述信息
     */
    var requestMappingConditions: RequestMappingConditionsDescription? = null
}