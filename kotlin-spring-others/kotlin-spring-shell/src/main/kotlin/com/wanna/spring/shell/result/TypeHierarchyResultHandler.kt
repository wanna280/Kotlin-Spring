package com.wanna.spring.shell.result

/**
 * 类型继承的ResultHandler
 */
open class TypeHierarchyResultHandler : ResultHandler<Any> {
    override fun handleResult(result: Any) {
        if (result is Throwable) {
            println(result.message)
        } else {
            println(result)
        }
    }
}