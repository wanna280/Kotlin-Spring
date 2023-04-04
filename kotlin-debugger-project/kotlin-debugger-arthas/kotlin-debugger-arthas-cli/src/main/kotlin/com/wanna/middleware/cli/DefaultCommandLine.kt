package com.wanna.middleware.cli

import com.wanna.middleware.cli.converter.Converters
import javax.annotation.Nullable

/**
 * CommandLine的默认实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/26
 *
 * @param cli 需要去进行描述的命令行CLI
 */
open class DefaultCommandLine(private val cli: CLI) : CommandLine {

    /**
     * 参数值列表
     */
    private val allArgs = ArrayList<String>()

    /**
     * Argument对应的原始参数值列表
     */
    private val argumentValues = LinkedHashMap<Argument, MutableList<String>>()

    /**
     * Option对应的原始参数值列表
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

    /**
     * 为给定的Option, 去添加一个选项参数值
     *
     * @param option Option
     * @param value 要去进行添加的选项值
     * @return this(DefaultCommandLine)
     */
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
        return getRawValuesForOption(option).isNotEmpty()
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
        return create(getRawValueForArgument(argument), argument as TypedArgument<T>)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getOptionValues(name: String): List<T>? {
        val option = cli.getOption(name) ?: return null
        if (option !is TypedOption<*>) {
            return getRawValuesForOption(option) as List<T>?
        }
        if (option.isParsedAsList()) {
            return createFromList(getRawValueForOption(option), option as TypedOption<T>)
        }
        return typedValues(option as TypedOption<T>, getRawValuesForOption(option))
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getArgumentValues(name: String): List<T>? {
        val argument = cli.getArgument(name) ?: return null
        if (argument !is TypedArgument<*>) {
            return getRawValuesForArgument(argument) as List<T>?
        }
        val rawValues = getRawValuesForArgument(argument)
        return typedValues(argument as TypedArgument<T>, rawValues)
    }

    /**
     * 检查给定的Option已经分配的原始参数值
     *
     * @param option Option
     * @return 如果该Option已经分配的参数值列表
     */
    override fun getRawValuesForOption(option: Option): List<String> {
        return rawValues(this.optionValues[option])
    }

    /**
     * 检查给定的Argument已经分配者的原始参数值
     *
     * @param argument Argument
     * @return 如果该Argument已经分配的参数值列表
     */
    override fun getRawValuesForArgument(argument: Argument): List<String> {
        return rawValues(argumentValues[argument])
    }

    /**
     * 为给定的Option去获取到已经分配的原始参数值, 如果该Option给定了多个参数值, 那么返回第一个;
     * 如果该Option参数值没有指定的话, 那么返回给Option的默认值
     *
     * @param option Option
     * @return 该Option对应的已经分配的原始参数值
     */
    @Nullable
    override fun getRawValueForOption(option: Option): String? {
        return if (isOptionAssigned(option)) return getRawValuesForOption(option)[0]
        else option.getDefaultValue()
    }

    /**
     * 为给定的Argument去获取到所有的原始参数值, 如果该Argument给定了多个参数值, 那么返回第一个;
     * 如果该Argument参数值没有指定的话, 那么返回该Argument的默认值
     *
     * @param argument Argument
     * @return 该Argument对应的已经分配的原始参数值
     */
    @Nullable
    override fun getRawValueForArgument(argument: Argument): String? {
        val values = argumentValues[argument]
        return if (!values.isNullOrEmpty()) values[0] else argument.getDefaultValue()
    }

    @Nullable
    private fun <T> getValue(option: TypedOption<T>): T? {
        if (this.isOptionAssigned(option)) {
            return create(getRawValueForOption(option), option)
        }
        if (option.getDefaultValue() != null) {
            return create(option.getDefaultValue(), option)
        }
        return null
    }

    private fun rawValues(@Nullable list: List<*>?): List<String> {
        return list?.map { it.toString() } ?: emptyList()
    }

    companion object {

        /**
         * 将给定的字符串列表参数值, 按照给定的类型, 去解析成为目标类型对象的列表
         *
         * @param typed 要去进行转换的目标类型信息
         * @param rawValues 原始的参数值字符串列表
         * @return 转换成为的目标对象列表
         */
        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        private fun <T> typedValues(typed: TypedArgument<T>, rawValues: List<String>): List<T> {
            val result = ArrayList<T>()
            for (rawValue in rawValues) {
                result.add(create(rawValue, typed) as T)
            }
            return result
        }

        /**
         * 根据给定的字符串, 按照分隔符去分割成为列表, 并转换成为目标类型的对象
         *
         * @param raw 待进行转换的字符串(将会使用分隔符去进行分割)
         * @param option 对于单个元素, 将要去转换成为的目标类型
         * @return 转换成为的目标对象列表
         */
        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        fun <T> createFromList(@Nullable raw: String?, option: TypedOption<T>): List<T> {
            raw ?: return emptyList()
            val segments = raw.split(option.getListSeparator())
            val result = ArrayList<T>()
            for (segment in segments) {
                result.add(create(segment, option) as T)
            }
            return result
        }

        @Suppress("UNCHECKED_CAST")
        @JvmStatic
        private fun <T> typedValues(typed: TypedOption<T>, rawValues: List<String>): List<T> {
            val result = ArrayList<T>()
            for (rawValue in rawValues) {
                result.add(create(rawValue, typed) as T)
            }
            return result
        }

        @Nullable
        @JvmStatic
        fun <T> create(@Nullable value: String?, option: TypedOption<T>): T? {
            val valueToUse = value ?: option.getDefaultValue()

            val converter = option.getConverter()
            if (converter != null) {
                return Converters.create(valueToUse, converter)
            }
            return Converters.create(option.getType(), valueToUse)
        }

        @Nullable
        @JvmStatic
        fun <T> create(@Nullable value: String?, argument: TypedArgument<T>): T? {
            val valueToUse = value ?: argument.getDefaultValue()

            val converter = argument.getConverter()
            if (converter != null) {
                return Converters.create(value, converter)
            }
            return Converters.create(argument.getType(), valueToUse)
        }
    }
}