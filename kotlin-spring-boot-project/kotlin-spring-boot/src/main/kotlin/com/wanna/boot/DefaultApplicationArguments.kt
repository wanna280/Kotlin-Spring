package com.wanna.boot

import com.wanna.framework.core.environment.SimpleCommandLinePropertySource

/**
 * 这是一个默认的ApplicationArguments实现
 *
 * @see ApplicationArguments
 */
open class DefaultApplicationArguments(private var args: Array<String>) : ApplicationArguments {
    val source = SimpleCommandLinePropertySource(*args)
    override fun getSourceArgs(): Array<String> = this.args
}