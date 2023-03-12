package com.wanna.middleware.cli

import javax.annotation.Nullable

/**
 * 对于一个命令行来说, 分为name/option/argument三个部分
 *
 * * 1.对于name, 也就是命令的名字, 对应的就是本地的二进制可执行文件.
 *
 * * 2.对于Option, 通常以'-'或者是'--'作为开头, 后面跟一个选项名称.
 * Option通常情况下, 都会有一个默认值, 因此不指定Option时将会使用默认值.
 * Option通常情况下, 只会有Boolean类型.
 *
 * * 3.对于Argument, 用于传递参数信息, 通常情况下没有默认值, Argument
 * 通常情况下都要求用户必须指定,如果不指定的话, 将会提示用户去进行输入.
 * Argument通常情况下, 可以是字符串/数字/文件路径等不同类型的数据.
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/25
 *
 * @see CLI
 * @see Argument
 */
open class Option {

    private var argName: String = DEFAULT_ARG_NAME

    private var longName: String = NO_NAME

    private var shortName: String = NO_NAME

    private var description: String = ""

    private var required: Boolean = false

    private var hidden: Boolean = false

    private var defaultValue: String = ""

    private var singleValued: Boolean = false

    private var multipleValued: Boolean = false

    open fun getArgName(): String {
        return this.argName
    }

    open fun setArgName(argName: String): Option {
        this.argName = argName
        return this
    }

    open fun getLongName(): String {
        return this.longName
    }

    open fun setLongName(longName: String): Option {
        this.longName = longName
        return this
    }

    open fun getShortName(): String {
        return this.shortName
    }

    open fun setShortName(shortName: String): Option {
        this.shortName = shortName
        return this
    }

    open fun getDescription(): String {
        return this.description
    }

    open fun setDescription(description: String): Option {
        this.description = description
        return this
    }

    open fun isRequired(): Boolean {
        return this.required
    }

    open fun setRequired(required: Boolean): Option {
        this.required = required
        return this
    }

    open fun isHidden(): Boolean {
        return this.hidden
    }

    open fun setHidden(hidden: Boolean): Option {
        this.hidden = hidden
        return this
    }

    open fun isSingleValued(): Boolean {
        return this.singleValued
    }

    open fun setSingleValued(singleValued: Boolean): Option {
        this.singleValued = singleValued
        return this
    }

    open fun isMultipleValued(): Boolean {
        return this.multipleValued
    }

    open fun setMultipleValued(multipleValued: Boolean): Option {
        this.multipleValued = multipleValued
        return this
    }

    open fun acceptValue(): Boolean {
        return this.singleValued || this.multipleValued
    }

    @Nullable
    open fun getDefaultValue(): String? {
        return this.defaultValue
    }

    open fun setDefaultValue(defaultValue: String): Option {
        this.defaultValue = defaultValue
        return this
    }

    /**
     * 检查参数值的合法性
     */
    open fun ensureValidity() {

    }


    companion object {
        /**
         * 默认的参数名
         */
        const val DEFAULT_ARG_NAME = "value"

        /**
         * 无名参数
         */
        const val NO_NAME = "\u0000"
    }

}