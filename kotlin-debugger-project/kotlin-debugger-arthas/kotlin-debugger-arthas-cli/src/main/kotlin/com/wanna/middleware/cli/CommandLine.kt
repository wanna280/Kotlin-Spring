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

    /**
     * 检查给定的Option是否还可以接收更多的参数值?
     *
     * @param option Option
     * @return 如果该Option还能接收更多参数值的话, return true; 否则return false
     */
    fun acceptMoreValues(option: Option): Boolean

    /**
     * 检查给定的Option是否已经分配了参数值?
     *
     * @param option Option
     * @return 如果该Option已经分配了参数值, return true; 否则return false
     */
    fun isOptionAssigned(option: Option): Boolean

    /**
     * 为给定的Option去获取到所有的原始参数值
     *
     * @param option Option
     * @return 该Option对应的原始参数值列表
     */
    fun getRawValuesForOption(option: Option): List<String>

    /**
     * 为给定的Argument去获取到所有的原始参数值
     *
     * @param argument Argument
     * @return 该Argument对应的原始参数值列表
     */
    fun getRawValuesForArgument(argument: Argument): List<String>

    /**
     * 为给定的Option去获取到已经分配的原始参数值, 如果该Option给定了多个参数值, 那么返回第一个;
     * 如果该Option参数值没有指定的话, 那么返回给Option的默认值
     *
     * @param option Option
     * @return 该Option对应的已经分配的原始参数值
     */
    @Nullable
    fun getRawValueForOption(option: Option): String?

    /**
     * 为给定的Argument去获取到所有的原始参数值, 如果该Argument给定了多个参数值, 那么返回第一个;
     * 如果该Argument参数值没有指定的话, 那么返回该Argument的默认值
     *
     * @param argument Argument
     * @return 该Argument对应的已经分配的原始参数值
     */
    @Nullable
    fun getRawValueForArgument(argument: Argument): String?
}