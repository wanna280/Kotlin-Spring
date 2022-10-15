package com.wanna.boot.actuate.endpoint.web.annotation

import com.wanna.boot.actuate.endpoint.EndpointId
import com.wanna.boot.actuate.endpoint.Operation
import com.wanna.boot.actuate.endpoint.annotation.EndpointDiscoverer
import com.wanna.boot.actuate.endpoint.invoke.OperationInvoker
import com.wanna.boot.actuate.endpoint.invoke.reflect.OperationMethod
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.core.annotation.AnnotatedElementUtils


/**
 * Controller的EndpointDiscovery
 */
open class ControllerEndpointDiscoverer(applicationContext: ApplicationContext) :
    EndpointDiscoverer<ExposableControllerEndpoint, Operation>(applicationContext), ControllerEndpointsSupplier {

    /**
     * 只去暴露标注了@ControllerEndpoint/@RestControllerEndpoint注解的Endpoint
     *
     * @param beanType beanType
     * @return 如果标注了@ControllerEndpoint/@RestControllerEndpoint，return true；否则return false
     */
    override fun isEndpointTypeExposed(beanType: Class<*>): Boolean {
        return AnnotatedElementUtils.hasAnnotation(beanType, ControllerEndpoint::class.java)
                || AnnotatedElementUtils.hasAnnotation(beanType, RestControllerEndpoint::class.java)
    }

    /**
     * 创建Endpoint，我们需要返回一个ControllerEndpoint，并且没有任何的Operation
     */
    override fun createEndpoint(
        id: EndpointId,
        endpointBean: Any,
        operations: Collection<Operation>
    ): ExposableControllerEndpoint {
        return DiscoveredControllerEndpoint(id, endpointBean, id.value)
    }

    /**
     * 创建一个OperationOperation？对于ControllerEndpoint，不应该存在有Endpoint
     */
    override fun createOperation(
        endpointId: EndpointId,
        operationMethod: OperationMethod,
        invoker: OperationInvoker
    ) = throw IllegalStateException("ControllerEndpoint不应该定义Operation")
}