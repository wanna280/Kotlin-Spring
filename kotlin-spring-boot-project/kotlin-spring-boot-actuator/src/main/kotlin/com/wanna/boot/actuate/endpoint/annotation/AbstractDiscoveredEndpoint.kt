package com.wanna.boot.actuate.endpoint.annotation

import com.wanna.boot.actuate.endpoint.AbstractExposableEndpoint
import com.wanna.boot.actuate.endpoint.EndpointId
import com.wanna.boot.actuate.endpoint.Operation

abstract class AbstractDiscoveredEndpoint<O : Operation>(
    endpointId: EndpointId,
    operations: Collection<O>,
    val endpointBean: Any
) : AbstractExposableEndpoint<O>(endpointId, operations) {

}