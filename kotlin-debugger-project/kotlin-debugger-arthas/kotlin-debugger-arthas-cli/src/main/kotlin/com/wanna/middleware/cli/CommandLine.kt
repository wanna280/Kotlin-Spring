package com.wanna.middleware.cli

import javax.annotation.Nullable

/**
 * 对于单个命令行参数列表的解析的结果
 *
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