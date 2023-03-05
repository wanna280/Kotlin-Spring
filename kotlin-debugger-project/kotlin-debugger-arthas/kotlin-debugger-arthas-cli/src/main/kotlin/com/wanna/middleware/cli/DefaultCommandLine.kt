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

    /**
     * Argument对应的原始参数值
     */
    private val argumentValues = LinkedHashMap<Argument, MutableList<String>>()

    /**
     * Option对应的原始参数值
     */
    private val optionValues = LinkedHashMap<Option, MutableList<String>>()

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

    open fun addRawValue(option: Option, value: String): DefaultCommandLine {
        var list = this.optionValues[option]
        if (list == null) {
            list = ArrayList()
            this.optionValues[option] = list
        }
        list.add(value)
        return this
    }

    /**
     * 检查给定的Option, 是否还需要接收更多的参数值?
     *
     * * 1.如果该Option接收多个参数, return true;
     * * 2.如果该Option接收单个参数, 并且该Option没有已经存在的参数值, return true
     *
     * @param option option
     */
    override fun acceptMoreValues(option: Option): Boolean {
        return option.isMultipleValued() || option.isSingleValued() && !this.isOptionAssigned(option)
    }

    @Suppress("UNCHECKED_CAST")
    @Nullable
    override fun <T> getOptionValue(name: String): T? {
        val option = this.cli.getOption(name) ?: return null
        return if (option is TypedOption<*>) return getValue(option as TypedOption<T>) else getRawValueForOption(option) as T
    }

    /**
     * 检查给定的Option是否已经分配了参数值?
     *
     * @param option Option
     * @return 如果该Option已经分配了参数值, return true; 否则return false
     */
    override fun isOptionAssigned(option: Option): Boolean {
        return getRawValueForOption(option).isEmpty()
    }

    @Nullable
    override fun <T> getArgumentValue(name: String): T? {
        val argument = this.cli.getArgument(name)
        return if (argument == null) null else this.getArgumentValue(argument.getIndex())
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getArgumentValue(index: Int): T? {
        val argument = this.cli.getArgument(index) ?: return null
        if (argument !is TypedArgument<*>) {
            return getRawValueForArgument(argument) as T?
        }
        // TODO
        return null
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getOptionValues(name: String): List<T>? {
        val option = cli.getOption(name) ?: return null
        if (option !is TypedOption<*>) {
            return getRawValuesForOption(option) as List<T>?
        }
        // TODO
        return null
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getArgumentValues(name: String): List<T>? {
        val argument = cli.getArgument(name) ?: return null
        if (argument !is TypedArgument<*>) {
            return getRawValuesForArgument(argument) as List<T>?
        }
        // TODO
        return null
    }

    /**
     * 检查给定的Option是否已经分配了参数值?
     *
     * @param option Option
     * @return 如果该Option已经分配了参数值, return true; 否则return false
     */
    override fun getRawValuesForOption(option: Option): List<String> {
        return rawValues(this.optionValues[option])
    }

    override fun getRawValuesForArgument(argument: Argument): List<String> {
        return rawValues(argumentValues[argument])
    }

    override fun getRawValueForOption(option: Option): String {
        TODO("Not yet implemented")
    }

    override fun getRawValueForArgument(argument: Argument): String {
        val values = argumentValues[argument]
        return if (!values.isNullOrEmpty()) values[0] else argument.getDefaultValue()
    }

    @Nullable
    private fun <T> getValue(option: TypedOption<T>): T? {
        return null!!
    }

    private fun rawValues(@Nullable list: List<*>?): List<String> {
        return list?.map { it.toString() } ?: emptyList()
    }
}