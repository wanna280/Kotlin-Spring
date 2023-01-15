package com.wanna.boot.actuate.endpoint.invoke.reflect

import com.wanna.boot.actuate.endpoint.invoke.OperationParameter
import com.wanna.boot.actuate.endpoint.invoke.OperationParameters
import com.wanna.framework.core.ParameterNameDiscoverer
import java.lang.reflect.Method
import java.util.stream.Stream

/**
 * OperationParameters的默认实现, 组合了OperationParameter的列表
 *
 * @param method 要维护的Operation方法
 * @param parameterNameDiscoverer 参数名发现器, 提供Operation方法的参数名的列表的获取
 *
 * @see OperationParameter
 */
open class OperationMethodParameters(
    private val method: Method,
    private val parameterNameDiscoverer: ParameterNameDiscoverer
) : OperationParameters {

    // 维护Operation的方法参数的列表
    private val parameters: MutableList<OperationParameter> = ArrayList()

    init {
        val parameterNames = parameterNameDiscoverer.getParameterNames(method)
            ?: throw IllegalStateException("无法获取到目标方法[${method.toGenericString()}]的参数名列表")
        parameterNames.indices.forEach {
            parameters.add(OperationMethodParameter(parameterNames[it], method.parameters[it]))
        }
    }

    override fun iterator(): Iterator<OperationParameter> = parameters.iterator()
    override fun getParameterCount() = parameters.count()
    override fun get(index: Int) = parameters[index]
    override fun stream(): Stream<OperationParameter> = parameters.stream()
}