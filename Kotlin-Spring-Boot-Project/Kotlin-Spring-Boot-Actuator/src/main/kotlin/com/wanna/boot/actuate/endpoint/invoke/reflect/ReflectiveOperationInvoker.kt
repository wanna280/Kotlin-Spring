package com.wanna.boot.actuate.endpoint.invoke.reflect

import com.wanna.boot.actuate.endpoint.InvocationContext
import com.wanna.boot.actuate.endpoint.invoke.MissingParametersException
import com.wanna.boot.actuate.endpoint.invoke.OperationInvoker
import com.wanna.boot.actuate.endpoint.invoke.OperationParameter
import com.wanna.framework.core.convert.support.DefaultConversionService
import com.wanna.framework.core.util.ReflectionUtils
import kotlin.jvm.Throws

/**
 * 反射执行目标Operation的方法的Invoker
 *
 * @param target 执行目标方法的target对象(EndpointBean)
 * @param operationMethod OperationType and TargetMethod
 */
open class ReflectiveOperationInvoker(private val target: Any, private val operationMethod: OperationMethod) :
    OperationInvoker {

    /**
     * 解析目标方法的参数，并反射执行目标Operation方法
     *
     * @param context 执行目标方法需要用到的Context信息，维护了执行该方法需要用到的参数列表
     * @return 执行Operation方法的返回值
     * @throws MissingParametersException 如果出现了某些必要的参数，没有给出的话
     */
    @Throws(MissingParametersException::class)
    override fun invoke(context: InvocationContext): Any? {
        // 验证方法参数的合法性，看是否缺少了其中的一些方法参数
        validateRequiredParameters(context)

        // 根据方法的参数名，从context当中解析方法的参数列表
        val args = resolveArguments(context)

        // 根据解析到的方法参数列表，去执行目标Operation方法
        ReflectionUtils.makeAccessible(operationMethod.method)
        return ReflectionUtils.invokeMethod(operationMethod.method, target, *args)
    }

    /**
     * 验证参数的合法性，判断是否有参数，该方法需要，但是并不存在于参数列表当中？
     *
     * @param context 方法参数列表
     * @throws MissingParametersException 如果缺少了某些必要的参数的话
     */
    @Throws(MissingParametersException::class)
    private fun validateRequiredParameters(context: InvocationContext) {
        val missedParameters =
            operationMethod.parameters.filter { isMissing(context, it) }.toList()
        if (missedParameters.isNotEmpty()) {
            throw MissingParametersException(missedParameters)
        }
    }

    /**
     * 如果"parameter.isMandatory=true"，说明不能为空，但是你给了null，就说明该参数missing
     *
     * @param context 本次请求当中的参数值列表
     * @param parameter 要去进行匹配的方法参数
     * @return isMissing？
     */
    private fun isMissing(context: InvocationContext, parameter: OperationParameter): Boolean {
        return parameter.isMandatory() && !context.containsArgument(parameter.getName())
    }

    /**
     * 解析目标方法的参数列表
     *
     * @param context 执行目标方法需要用到的Context信息，维护了执行该方法需要用到的参数列表
     * @return 执行目标方法，需要用到的参数列表(Array)
     */
    private fun resolveArguments(context: InvocationContext): Array<Any?> {
        return operationMethod.parameters.map { resolveArguments(context, it) }.toTypedArray()
    }

    /**
     * 解析目标方法参数的具体的值，如果必要的话，使用Converter去完成类型转换工作
     *
     * @param context 执行目标Operation方法需要用到的Context信息，维护了执行该方法需要用到的参数列表
     * @param parameter 目标Operation方法的一个参数
     */
    private fun resolveArguments(context: InvocationContext, parameter: OperationParameter): Any? {
        val value = context.getArgument(parameter.getName())
        val conversionService = DefaultConversionService.getSharedInstance()
        return conversionService.convert(value, parameter.getType())
    }
}