package com.wanna.boot.actuate.endpoint.web.mvc

import com.wanna.boot.actuate.endpoint.web.EndpointLinksResolver
import com.wanna.boot.actuate.endpoint.web.EndpointMapping
import com.wanna.boot.actuate.endpoint.web.ExposableWebEndpoint
import com.wanna.boot.actuate.endpoint.web.Link
import com.wanna.framework.web.method.annotation.ResponseBody
import com.wanna.framework.web.server.HttpServerRequest
import com.wanna.framework.web.server.HttpServerResponse

/**
 * WebMvc的Endpoint的HandlerMapping
 */
open class WebMvcEndpointHandlerMapping(
    endpoints: List<ExposableWebEndpoint>,
    endpointMapping: EndpointMapping,
    shouldRegisterLinksMapping: Boolean,
    val linksResolver: EndpointLinksResolver
) : AbstractWebMvcEndpointHandlerMapping(endpoints, endpointMapping, shouldRegisterLinksMapping) {

    // 获取Links的Handler
    override fun getLinksHandler(): WebMvcLinksHandler = WebMvcLinksHandler()

    protected inner class WebMvcLinksHandler : LinksHandler {
        @ResponseBody
        override fun links(request: HttpServerRequest, response: HttpServerResponse): Map<String, Map<String, Link>> {
            return mapOf("_links" to linksResolver.resolveLinks(request.getLocalHost() + request.getUrl()))
        }

        override fun toString() = "Actuator root web endpoint"
    }
}