package com.wanna.middleware.cli

import javax.annotation.Nullable

/**
 * 对于单个命令行参数列表的解析的结果
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/25
 */
interface CommandLine {
    fun cli(): CLI

    fun allArguments(): List<String>

    @Nullable
    fun <T> getOptionValue(name: String): T?

    @Nullable
    fun <T> getArgumentValue(name: String): T?

    @Nullable
    fun <T> getArgumentValue(index: Int): T?

    @Nullable
    fun <T> getOptionValues(name: String): List<T>?

    @Nullable
    fun <T> getArgumentValues(name: String): List<T>?

    @Nullable
    fun getRawValueForOption(option: Option): String?

    @Nullable
    fun getRawValueForArgument(argument: Argument): String?
}