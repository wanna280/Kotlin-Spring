package com.wanna.middleware.cli

import com.wanna.middleware.cli.converter.Converter

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/25
 */
open class TypedOption<T> : Option() {

    private var type: Class<T>? = null

    private var parsedAsList: Boolean = false

    private var listSeparator: String = ","

    private var converter: Converter<T>? = null

    private var defaultValue: String? = null

    open fun setType(type: Class<T>): TypedOption<T> {
        this.type = type
        return this
    }

    open fun getType(): Class<T> {
        return this.type ?: throw IllegalStateException("type has not been initialized")
    }

    open fun isParsedAsList(): Boolean {
        return parsedAsList
    }

    open fun setParsedAsList(parsedAsList: Boolean): TypedOption<T> {
        this.parsedAsList = parsedAsList
        return this
    }

    open fun getListSeparator(): String {
        return this.listSeparator
    }

    open fun setListSeparator(listSeparator: String): TypedOption<T> {
        this.listSeparator = listSeparator
        return this
    }

    open fun getConverter(): Converter<T> {
        return this.converter ?: throw IllegalStateException("Converter has not been initialized")
    }

    open fun setConverter(converter: Converter<T>): TypedOption<T> {
        this.converter = converter
        return this
    }

    open fun getDefaultValue(): String {
        return this.defaultValue ?: throw IllegalStateException("defaultValue has not been initialized")
    }

    open fun setDefaultValue(defaultValue: String): TypedOption<T> {
        this.defaultValue = defaultValue
        return this
    }
}