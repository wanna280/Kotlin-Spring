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
     * 设置CLI命令的名字commandName
     *
     * @param name command name
     */
    fun setName(name: String): CLI

    /**
     * 获取CLI命令的commandName
     *
     * @return commandName
     */
    fun getName(): String

    /**
     * 设置当前CLI命令的描述信息
     *
     * @param description 描述信息
     * @return this
     */
    fun setDescription(description: String): CLI

    /**
     * 获取当前CLI命令的描述信息
     *
     * @return 当前CLI命令的描述信息
     */
    fun getDescription(): String

    fun setSummary(summary: String): CLI

    fun getSummary(): String

    fun setHidden(hidden: Boolean): CLI

    fun isHidden(): Boolean

    /**
     * 命令是否要忽略大小写? true代表区分大小写, false代表区分大小写
     *
     * @return 是否大小写敏感?
     */
    fun isCaseSensitive(): Boolean

    /**
     * 设置当前的命令是否要区分大小写
     *
     * @param caseSensitive 当前命令是否区分大小写?
     */
    fun setCaseSensitive(caseSensitive: Boolean): CLI

    // -------------------------Option相关API---------------------------------

    fun addOption(option: Option): CLI

    fun addOptions(options: List<Option>): CLI

    fun setOptions(options: List<Option>): CLI

    fun removeOption(name: String): CLI

    @Nullable
    fun getOption(name: String): Option?

    fun getOptions(): List<Option>

    // -------------------------Argument相关API---------------------------------

    fun addArgument(argument: Argument): CLI

    fun addArguments(arguments: List<Argument>): CLI

    fun setArguments(arguments: List<Argument>): CLI

    @Nullable
    fun getArgument(index: Int): Argument?

    @Nullable
    fun getArgument(name: String): Argument?

    fun removeArgument(index: Int): CLI

    fun getArguments(): List<Argument>

    // -------------------------Usage相关API---------------------------------

    fun usage(builder: StringBuilder): CLI

    fun usage(builder: StringBuilder, prefix: String): CLI

    fun usage(builder: StringBuilder, @Nullable formatter: UsageMessageFormatter?): CLI
}