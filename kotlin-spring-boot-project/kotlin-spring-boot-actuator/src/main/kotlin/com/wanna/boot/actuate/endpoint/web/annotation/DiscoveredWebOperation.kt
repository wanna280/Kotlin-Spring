package com.wanna.boot.actuate.endpoint.web.annotation

import com.wanna.boot.actuate.endpoint.EndpointId
import com.wanna.boot.actuate.endpoint.InvocationContext
import com.wanna.boot.actuate.endpoint.annotation.Selector
import com.wanna.boot.actuate.endpoint.invoke.OperationInvoker
import com.wanna.boot.actuate.endpoint.invoke.reflect.OperationMethod
import com.wanna.boot.actuate.endpoint.web.WebOperation
import com.wanna.boot.actuate.endpoint.web.WebOperationRequestPredicate
import java.lang.StringBuilder
import java.lang.reflect.Method
import java.lang.reflect.Parameter

open class DiscoveredWebOperation(
    private val endpointId: EndpointId,
    private val requestPredicate: WebOperationRequestPredicate,
    private val operationMethod: OperationMethod,
    private val invoker: OperationInvoker
) : WebOperation {

    // OperationId
    private var id: String = getId(endpointId, operationMethod)

    /**
     * 获取该Operation的Id, 根据endpointId和方法参数名去进行拼接
     *
     * Note: 这里Spring当中是直接使用的parameter.name作为的参数名, 但是很可惜, 我们这里是Kotlin, 暂时无办法获取到,
     * 我们需要把它映射到对应的OperationParameter当中, 因为OperationParameter使用了参数名发现器去获取, 因此可以获取到
     *
     * @param endpointId endpointId
     * @param operationMethod OperationType and Method
     * @return OperationId
     */
    private fun getId(endpointId: EndpointId, operationMethod: OperationMethod): String {
        val parameters = operationMethod.method.parameters
        return endpointId.value + parameters.indices
            .filter { hasSelector(parameters[it]) }
            .joinToString { "-" + operationMethod.parameters.get(it).getName() }
    }

    /**
     * 获取该Operation的类型, READ/WRITE/DELETE
     *
     * @return OperationType
     */
    override fun getType() = operationMethod.operationType

    /**
     * 执行目标Operation方法, 直接交给对应的Invoker去进行执行
     *
     * @param context InvocationContext(执行目标方法需要用到的参数列表)
     * @return 交给Invoker执行Operation方法的最终执行结果
     */
    override fun invoke(context: InvocationContext) = invoker.invoke(context)

    /**
     * 获取当前的Operation的Id
     *
     * @return operationId
     */
    override fun getId(): String = this.id

    /**
     * 获取该Operation方法的请求断言, 支持去对路径, 请求方式等去进行匹配1
     *
     * @return 请求断言
     */
    override fun getRequestPredicate() = this.requestPredicate

    /**
     * 获取到加了破折号(dash)的parameterName
     *
     * @param parameter 要添加破折号的方法参数
     * @return 将方法名添加了破折号之后的字符串
     */
    private fun dashName(parameter: Parameter): String = "-" + parameter.name

    /**
     * 判断目标方法上是否有@Selector注解? 
     *
     * @param parameter 要匹配@Selector注解的方法参数
     * @return 如果该方法参数有@Selector注解, 那么return true, 否则return false
     */
    private fun hasSelector(parameter: Parameter): Boolean = parameter.getAnnotation(Selector::class.java) != null
}