package com.wanna.middleware.cli

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/25
 */
open class TypedArgument<T> : Argument() {

    private var type: Class<T>? = null

    open fun getType(): Class<T> {
        return this.type ?: throw IllegalStateException("type has not been initialized")
    }

    open fun setType(type: Class<T>): TypedArgument<T> {
        this.type = type
        return this
    }

}