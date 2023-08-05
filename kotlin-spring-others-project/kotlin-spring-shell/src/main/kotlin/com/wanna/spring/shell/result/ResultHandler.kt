package com.wanna.spring.shell.result

/**
 * Shell的执行结果的处理器
 */
interface ResultHandler<T> {
    fun handleResult(result: T)
}