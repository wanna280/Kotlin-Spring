package com.wanna.boot.actuate.endpoint.web.annotation

import com.wanna.boot.actuate.endpoint.EndpointId
import com.wanna.boot.actuate.endpoint.Operation
import com.wanna.boot.actuate.endpoint.annotation.AbstractDiscoveredEndpoint
import com.wanna.boot.actuate.endpoint.web.PathMappedEndpoint

/**
 * Controller的Endpoint
 */
class DiscoveredControllerEndpoint(
    endpointId: EndpointId,
    endpointBean: Any,
    private val rootPath: String
) : AbstractDiscoveredEndpoint<Operation>(endpointId, emptyList(), endpointBean),
    ExposableControllerEndpoint {
    override fun getController() = this.endpointBean
    override fun getRootPath() = rootPath
}