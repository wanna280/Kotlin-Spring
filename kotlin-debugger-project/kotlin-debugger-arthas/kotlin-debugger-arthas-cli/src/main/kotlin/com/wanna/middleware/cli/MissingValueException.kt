package com.wanna.middleware.cli

import javax.annotation.Nullable

/**
 * 缺失值的异常
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/3/6
 */
open class MissingValueException @JvmOverloads constructor(
    @Nullable message: String?,
    @Nullable cause: Throwable?
) : CLIException(message, cause) {

    var option: Option? = null
        private set

    var argument: Argument? = null
        private set

    constructor(option: Option) : this(null, null) {
        this.option = option
        this.argument = null
    }

    constructor(argument: Argument):this(null,null) {
        this.argument = argument
        this.option = null
    }

}