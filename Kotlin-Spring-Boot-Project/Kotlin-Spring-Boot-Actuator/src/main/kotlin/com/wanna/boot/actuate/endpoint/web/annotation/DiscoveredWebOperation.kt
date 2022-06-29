package com.wanna.boot.actuate.endpoint.web.annotation

import com.wanna.boot.actuate.endpoint.EndpointId
import com.wanna.boot.actuate.endpoint.InvocationContext
import com.wanna.boot.actuate.endpoint.invoke.OperationInvoker
import com.wanna.boot.actuate.endpoint.invoke.reflect.OperationMethod
import com.wanna.boot.actuate.endpoint.web.WebOperation
import com.wanna.boot.actuate.endpoint.web.WebOperationRequestPredicate

open class DiscoveredWebOperation(
    private val id: EndpointId,
    private val requestPredicate: WebOperationRequestPredicate,
    private val operationMethod: OperationMethod,
    private val invoker: OperationInvoker
) : WebOperation {
    override fun getType() = operationMethod.operationType

    /**
     * 执行目标Operation方法
     *
     * @param context InvocationContext
     * @return Operation方法的执行结果
     */
    override fun invoke(context: InvocationContext): Any? {
        return invoker.invoke(context)
    }

    override fun getId(): String = id.value

    override fun getRequestPredicate() = requestPredicate
}