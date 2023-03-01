package com.wanna.middleware.cli

import com.wanna.middleware.cli.converter.Converter

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/25
 */
open class TypedArgument<T> : Argument() {

    private var type: Class<T>? = null

    private var converter: Converter<T>? = null

    private var defaultValue: String? = null

    open fun getType(): Class<T> {
        return this.type ?: throw IllegalStateException("type has not been initialized")
    }

    open fun setType(type: Class<T>): TypedArgument<T> {
        this.type = type
        return this
    }

    open fun getConverter(): Converter<T> {
        return this.converter ?: throw IllegalStateException("Converter has not been initialized")
    }

    open fun setConverter(converter: Converter<T>): TypedArgument<T> {
        this.converter = converter
        return this
    }

    open fun getDefaultValue(): String {
        return this.defaultValue ?: throw IllegalStateException("defaultValue has not been initialized")
    }

    open fun setDefaultValue(defaultValue: String): TypedArgument<T> {
        this.defaultValue = defaultValue
        return this
    }

}