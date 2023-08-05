package com.wanna.middleware.cli

import javax.annotation.Nullable

/**
 * 缺失Option的异常
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/3/6
 */
open class MissingOptionException @JvmOverloads constructor(
    @Nullable message: String? = null,
    @Nullable cause: Throwable? = null
) : CLIException(message, cause) {

    var expected: Collection<Option>? = null
        private set

    constructor(expected: Collection<Option>) : this(null, null) {
        this.expected = expected
    }
}