package com.wanna.middleware.cli

import javax.annotation.Nullable

/**
 * CommandLine的默认实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/26
 */
open class DefaultCommandLine(private val cli: CLI) : CommandLine {

    private val allArgs = ArrayList<String>()

    private val argumentValues = LinkedHashMap<Argument, List<String>>()

    override fun cli(): CLI {
        return cli
    }

    override fun allArguments(): List<String> {
        return this.allArgs
    }

    open fun addArgumentValue(argument: String): CommandLine {
        this.allArgs.add(argument)
        return this
    }

    @Suppress("UNCHECKED_CAST")
    @Nullable
    override fun <T> getOptionValue(name: String): T? {
        val option = this.cli.getOption(name) ?: return null
        return if (option is TypedOption<*>) return getValue(option as TypedOption<T>) else getRawValueForOption(option) as T
    }

    override fun <T> getArgumentValue(name: String): T? {
        TODO("Not yet implemented")
    }

    override fun <T> getArgumentValue(index: Int): T? {
        TODO("Not yet implemented")
    }

    override fun <T> getOptionValues(name: String): List<T>? {
        TODO("Not yet implemented")
    }

    override fun <T> getArgumentValues(name: String): List<T>? {
        TODO("Not yet implemented")
    }

    override fun getRawValueForOption(option: Option): String? {
        TODO("Not yet implemented")
    }

    override fun getRawValueForArgument(argument: Argument): String? {
        val list = argumentValues[argument]
        return if (list.isNullOrEmpty()) argument.getDefaultValue() else list[0].toString()
    }

    @Nullable
    private fun <T> getValue(option: TypedOption<T>): T? {
        return null!!
    }
}