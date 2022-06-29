package com.wanna.boot.actuate.endpoint.annotation

import com.wanna.boot.actuate.endpoint.EndpointId
import com.wanna.boot.actuate.endpoint.OperationType
import com.wanna.boot.actuate.endpoint.invoke.OperationInvoker
import com.wanna.boot.actuate.endpoint.invoke.reflect.OperationMethod
import com.wanna.boot.actuate.endpoint.web.*
import com.wanna.boot.actuate.endpoint.web.annotation.DiscoveredWebEndpoint
import com.wanna.boot.actuate.endpoint.web.annotation.DiscoveredWebOperation
import com.wanna.framework.context.ApplicationContext

/**
 * Web的Endpoint的发现器，提供@Endpoint注解的匹配
 */
open class WebEndpointDiscoverer(applicationContext: ApplicationContext) :
    EndpointDiscoverer<ExposableWebEndpoint, WebOperation>(applicationContext), WebEndpointsSupplier {

    override fun createEndpoint(
        id: EndpointId,
        endpointBean: Any,
        operations: Collection<WebOperation>
    ): ExposableWebEndpoint {
        return DiscoveredWebEndpoint(id, id.value, operations)
    }

    override fun createOperation(id: EndpointId, method: OperationMethod, invoker: OperationInvoker): WebOperation {
        val requestPredicate = WebOperationRequestPredicate()
        requestPredicate.setPath(id.value)
        if (method.operationType == OperationType.READ) {
            requestPredicate.setHttpMethod(WebEndpointHttpMethod.GET)
        } else if (method.operationType == OperationType.WRITE) {
            requestPredicate.setHttpMethod(WebEndpointHttpMethod.POST)
        } else if (method.operationType == OperationType.DELETE) {
            requestPredicate.setHttpMethod(WebEndpointHttpMethod.DELETE)
        }

        return DiscoveredWebOperation(id, requestPredicate, method, invoker)
    }
}