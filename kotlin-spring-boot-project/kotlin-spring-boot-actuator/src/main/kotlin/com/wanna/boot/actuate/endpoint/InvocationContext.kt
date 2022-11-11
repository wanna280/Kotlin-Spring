package com.wanna.boot.actuate.endpoint

/**
 * 执行Endpoint方法的Context，负责执行Endpoint的Operation方法的相关参数列表
 *
 * @param arguments 执行目标的Operation方法需要的参数列表
 */
open class InvocationContext(private val arguments: Map<String, Any>) {
    open fun getArgument(name: String): Any? = arguments[name]
    open fun containsArgument(name: String): Boolean = arguments.containsKey(name)
}