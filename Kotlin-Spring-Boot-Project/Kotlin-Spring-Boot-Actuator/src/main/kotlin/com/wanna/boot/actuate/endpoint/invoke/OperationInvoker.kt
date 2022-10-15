package com.wanna.boot.actuate.endpoint.invoke

import com.wanna.boot.actuate.endpoint.InvocationContext

/**
 * Operation的Invoker，负责去执行目标Operation方法
 */
interface OperationInvoker {
    fun invoke(context: InvocationContext): Any?
}