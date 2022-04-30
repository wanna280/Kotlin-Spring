package com.wanna.boot

/**
 * 这是一个默认的ApplicationArguments实现
 *
 * @see ApplicationArguments
 */
open class DefaultApplicationArguments(private var args: Array<String>) : ApplicationArguments {

    override fun getSourceArgs(): Array<String> {
        return this.args
    }
}