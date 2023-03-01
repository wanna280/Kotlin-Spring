package com.wanna.middleware.cli

/**
 * 对于一个命令行来说, 分为name/option/argument三个部分
 *
 * 比如下面的命令当中:
 *
 * ```sh
 * java -jar xxx.jar
 * ```
 *
 * 其中, "java"是命令名, "-jar"为option(option一般以'-'或者是'--'作为开头), "xx.jar"为argument
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

    private var defaultValue: String = ""

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

    open fun getDefaultValue(): String {
        return this.defaultValue
    }

    /**
     * 检查参数值的合法性
     */
    open fun ensureValidity() {

    }
}