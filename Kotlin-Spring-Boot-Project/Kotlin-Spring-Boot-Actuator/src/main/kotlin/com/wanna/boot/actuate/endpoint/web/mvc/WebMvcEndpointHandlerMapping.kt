package com.wanna.boot.actuate.endpoint.web.mvc

import com.wanna.boot.actuate.endpoint.web.EndpointMapping
import com.wanna.boot.actuate.endpoint.web.ExposableWebEndpoint

/**
 * WebMvc的Endpoint的HandlerMapping
 */
open class WebMvcEndpointHandlerMapping(
    endpoints: List<ExposableWebEndpoint>,
    endpointMapping: EndpointMapping
) : AbstractWebMvcEndpointHandlerMapping(endpoints, endpointMapping) {

}