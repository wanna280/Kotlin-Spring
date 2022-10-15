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
 *
 * @see WebEndpointsSupplier
 */
open class WebEndpointDiscoverer(applicationContext: ApplicationContext) :
    EndpointDiscoverer<ExposableWebEndpoint, WebOperation>(applicationContext), WebEndpointsSupplier {

    /**
     * 告诉父类，我应该如何去创建一个Endpoint
     *
     * @param id endpointId
     * @param endpointBean endpointBean
     * @param operations 一个Endpoint下的所有的Operation列表
     */
    override fun createEndpoint(
        id: EndpointId,
        endpointBean: Any,
        operations: Collection<WebOperation>
    ): ExposableWebEndpoint {
        return DiscoveredWebEndpoint(id, id.value, operations, endpointBean)
    }

    /**
     * 告诉父类，我应该如何去创建一个Operation
     *
     * @param endpointId endpointId
     * @param operationMethod OperationType and Method
     * @param invoker 执行目标Operation方法的Invoker
     * @return 封装好的WebOperation
     */
    override fun createOperation(
        endpointId: EndpointId,
        operationMethod: OperationMethod,
        invoker: OperationInvoker
    ): WebOperation {
        // 根据该方法的OperationType，去获取到对应的HttpMethod
        val httpMethod = getHttpMethod(operationMethod.operationType)

        // 获取到该Operation的映射路径(将@Selector注解的参数，去作为路径变量拼接到path当中)
        val path = getPath(endpointId, operationMethod)

        // 根据path和httpMethod，去创建RequestPredicate
        val requestPredicate = WebOperationRequestPredicate(path, httpMethod)

        // 构建DiscoveredWebOperation
        return DiscoveredWebOperation(endpointId, requestPredicate, operationMethod, invoker)
    }

    private fun getHttpMethod(operationType: OperationType): WebEndpointHttpMethod {
        return when (operationType) {
            OperationType.READ -> WebEndpointHttpMethod.GET
            OperationType.WRITE -> WebEndpointHttpMethod.POST
            OperationType.DELETE -> WebEndpointHttpMethod.DELETE
        }
    }

    private fun getPath(endpointId: EndpointId, operationMethod: OperationMethod): String {
        val path = StringBuilder(endpointId.value)
        // Note: 这里Spring当中是直接使用的parameter.name作为的参数名，但是很可惜，我们这里是Kotlin，暂时无办法获取到
        // 我们需要把它映射到对应的OperationParameter当中，因为OperationParameter使用了参数名发现器去获取，因此可以获取到
        val parameters = operationMethod.method.parameters
        parameters.indices.filter { parameters[it].getAnnotation(Selector::class.java) != null }.forEach {
            path.append("/{").append(operationMethod.parameters.get(it).getName()).append("}")
        }
        return path.toString()
    }
}