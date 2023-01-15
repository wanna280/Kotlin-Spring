package com.wanna.boot.actuate.endpoint.invoke.reflect

import com.wanna.boot.actuate.endpoint.OperationType
import com.wanna.framework.core.DefaultParameterNameDiscoverer
import java.lang.reflect.Method

/**
 * Endpoint的Operation方法, 维护了该方法的OperationType以及要去进行执行的目标方法
 *
 * @param operationType OperationType(READ/WRITE/DELETE)
 * @param method 目标Operation方法
 */
class OperationMethod(val operationType: OperationType, val method: Method) {
    // Operation方法的参数列表, 将每个参数封装成为一个OperationParameter
    val parameters = OperationMethodParameters(method, DefaultParameterNameDiscoverer())
}