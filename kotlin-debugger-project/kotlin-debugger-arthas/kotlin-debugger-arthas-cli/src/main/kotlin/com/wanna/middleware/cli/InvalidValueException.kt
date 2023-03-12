package com.wanna.middleware.cli

import javax.annotation.Nullable

/**
 * 不合法的参数值异常
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/3/6
 */
open class InvalidValueException @JvmOverloads constructor(
    message: String? = null,
    cause: Throwable? = null
) : CLIException(message, cause) {

    var option: Option? = null
        private set

    var argument: Argument? = null
        private set

    constructor(option: Option, value: String, @Nullable cause: Throwable?) : this(
        "", cause
    ) {
        this.option = option
        this.argument = null
    }

    constructor(argument: Argument, value: String, @Nullable cause: Throwable?) : this(
        "", cause
    ) {

        this.argument = argument
        this.option = null
    }

}