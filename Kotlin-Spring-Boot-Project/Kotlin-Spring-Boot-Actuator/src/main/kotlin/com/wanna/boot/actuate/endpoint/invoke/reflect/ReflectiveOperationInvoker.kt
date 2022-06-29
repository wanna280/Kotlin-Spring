package com.wanna.boot.actuate.endpoint.invoke.reflect

import com.wanna.boot.actuate.endpoint.InvocationContext
import com.wanna.boot.actuate.endpoint.invoke.OperationInvoker

/**
 * 反射执行目标Operation的方法的Invoker
 *
 * @param target target对象
 */
open class ReflectiveOperationInvoker(val target: Any, val method: OperationMethod) : OperationInvoker {
    override fun invoke(context: InvocationContext): Any? {
        return method.method.invoke(target)
    }
}