package com.wanna.boot.actuate.endpoint

/**
 * 执行Endpoint方法的Context
 *
 * @param arguments 方法的参数列表
 */
open class InvocationContext(arguments: Map<String, Any>) {
    // 参数列表
    val arguments = HashMap<String, Any>()

    init {
        this.arguments.putAll(arguments)
    }

}