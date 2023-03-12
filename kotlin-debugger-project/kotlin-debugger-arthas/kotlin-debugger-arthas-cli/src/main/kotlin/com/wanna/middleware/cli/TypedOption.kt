package com.wanna.middleware.cli

import com.wanna.middleware.cli.converter.Converter
import javax.annotation.Nullable

/**
 * 支持去将Option的字符串字符串, 去转换成为目标类型的Converter
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/25
 */
open class TypedOption<T> : Option() {

    /**
     * 需要去将字符串去转换成为的目标类型
     */
    private var type: Class<T>? = null

    /**
     * 是否需要将单个参数值, 使用","的方式去拆分成为列表去进行解析
     */
    private var parsedAsList: Boolean = false

    /**
     * 将参数值拆分成为列表所使用的分隔符, 默认为","
     */
    private var listSeparator: String = ","

    /**
     * 用于去将字符串去转换成为目标类型的Converter
     */
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

    open fun getConverter(): Converter<T>? {
        return this.converter
    }

    open fun setConverter(converter: Converter<T>): TypedOption<T> {
        this.converter = converter
        return this
    }

    @Nullable
    override fun getDefaultValue(): String? {
        return this.defaultValue
    }

    override fun setDefaultValue(defaultValue: String): TypedOption<T> {
        this.defaultValue = defaultValue
        return this
    }
}