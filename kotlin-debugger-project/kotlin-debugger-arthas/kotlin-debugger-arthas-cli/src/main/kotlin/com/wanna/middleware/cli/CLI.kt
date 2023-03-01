package com.wanna.middleware.cli

import javax.annotation.Nullable

/**
 * CLI命令行的相关参数的模板信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/26
 */
interface CLI {

    /**
     * 解析给定的命令行参数
     *
     * @param args 命令行参数列表
     */
    fun parse(args: List<String>): CommandLine

    /**
     * 解析给定的命令行参数
     *
     * @param args 命令行参数列表
     */
    fun parse(args: List<String>, validate: Boolean): CommandLine

    /**
     * 设置CLI命令的名字
     *
     * @param name command name
     */
    fun setName(name: String): CLI

    fun getName(): String

    fun setDescription(description: String): CLI

    fun getDescription(): String

    fun setSummary(summary: String): CLI

    fun getSummary(): String

    fun setHidden(hidden: Boolean): CLI

    fun isHidden(): Boolean

    fun isCaseSensitive(): Boolean

    fun setCaseSensitive(caseSensitive: Boolean): CLI

    fun addOption(option: Option): CLI

    fun addOptions(options: List<Option>): CLI

    fun setOptions(options: List<Option>): CLI

    fun removeOption(name: String): CLI

    @Nullable
    fun getOption(name: String): Option?

    fun getOptions(): List<Option>

    fun addArgument(argument: Argument): CLI

    fun addArguments(arguments: List<Argument>): CLI

    fun setArguments(arguments: List<Argument>): CLI

    @Nullable
    fun getArgument(index: Int): Argument?

    @Nullable
    fun getArgument(name: String): Argument?

    fun removeArgument(index: Int): CLI

    fun getArguments(): List<Argument>

    fun usage(builder: StringBuilder): CLI

    fun usage(builder: StringBuilder, prefix: String): CLI

    fun usage(builder: StringBuilder, @Nullable formatter: UsageMessageFormatter?): CLI
}