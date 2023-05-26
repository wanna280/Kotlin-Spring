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
 * @see Option
 */
open class Argument {

    private var index: Int = -1

    private var argName: String = ""

    private var description: String = ""

    private var hidden: Boolean = false

    private var required: Boolean = false

    private var defaultValue: String? = null

    open fun setIndex(index: Int): Argument {
        this.index = index
        return this
    }

    open fun getIndex(): Int {
        return this.index
    }

    open fun setArgName(name: String): Argument {
        this.argName = name
        return this
    }

    open fun getArgName(): String {
        return this.argName
    }

    open fun isHidden(): Boolean {
        return this.hidden
    }

    open fun setHidden(hidden: Boolean): Argument {
        this.hidden = hidden
        return this
    }

    open fun getDescription(): String {
        return this.description
    }

    open fun setDescription(description: String): Argument {
        this.description = description
        return this
    }

    open fun isRequired(required: Boolean): Boolean {
        return this.required
    }

    open fun setRequired(required: Boolean): Argument {
        this.required = required
        return this
    }

    open fun setDefaultValue(defaultValue: String): Argument {
        this.defaultValue = defaultValue
        return this
    }

    @Nullable
    open fun getDefaultValue(): String? {
        return this.defaultValue
    }

    /**
     * 检查参数值的合法性
     */
    open fun ensureValidity() {

    }
}