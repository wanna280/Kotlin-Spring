package com.wanna.boot.actuate.web.mappings.mvc

import com.wanna.framework.web.DispatcherHandler

/**
 * 对于一个[DispatcherHandler]的HandlerMapping当中的Mapping(例如一个RequestMappingInfo)去进行描述
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/30
 *
 * @param handler 处理请求的Handler
 * @param predicate 请求到达该Handler的条件
 * @param details 对于Handler和Predicate的更多的具体细节
 */
data class DispatcherHandlerMappingDescription(
    val handler: String,
    val predicate: String,
    val details: DispatcherHandlerMappingDetails
)