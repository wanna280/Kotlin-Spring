package com.wanna.boot.actuate.endpoint.web.annotation

import com.wanna.boot.actuate.endpoint.EndpointId
import com.wanna.boot.actuate.endpoint.annotation.AbstractDiscoveredEndpoint
import com.wanna.boot.actuate.endpoint.web.ExposableWebEndpoint
import com.wanna.boot.actuate.endpoint.web.WebOperation

open class DiscoveredWebEndpoint(
    endpointId: EndpointId,
    val rootPath: String,
    operations: Collection<WebOperation>,
    endpointBean: Any
) : AbstractDiscoveredEndpoint<WebOperation>(endpointId, operations, endpointBean),
    ExposableWebEndpoint {

}