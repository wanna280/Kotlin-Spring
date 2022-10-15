package com.wanna.framework.core.environment

import com.wanna.framework.util.LinkedMultiValueMap

/**
 * 维护了命令行参数的信息
 */
class CommandLineArgs {
    // 没有option的参数列表
    private val nonOptionArgs = ArrayList<String>()

    // 设置了option的参数列表
    private val optionArgs = LinkedMultiValueMap<String, String>()

    // 添加optionArg参数
    fun addOptionArg(optionName: String, optionValue: String?) {
        if (!optionArgs.containsKey(optionName)) {
            optionArgs[optionName] = ArrayList()
        }
        if (optionValue != null) {
            optionArgs[optionName]!! += optionValue
        }
    }

    // 获取参数名列表
    fun getOptionNames(): List<String> = ArrayList(optionArgs.keys)

    // 判断是否存在这样的option参数
    fun containsOption(name: String): Boolean = optionArgs.containsKey(name)

    // 根据optionName去获取参数的配置信息
    fun getOptionValues(optionName: String): List<String?>? = optionArgs[optionName]

    // 添加没有option的参数
    fun addNonArg(arg: String) {
        this.nonOptionArgs += arg
    }

    // 获取没有option的参数列表
    fun getNonOptionArgs(): List<String> = this.nonOptionArgs
}