package com.wanna.framework.core.environment

/**
 * 这是一个简单的命令行参数的解析器的工具类实现, 对传递的命令行参数去进行解析成为[CommandLineArgs]
 *
 * @see CommandLineArgs
 * @see SimpleCommandLinePropertySource
 */
object SimpleCommandLineArgsParser {

    /**
     * 将给定的命令参数, 去进行解析成为[CommandLineArgs]
     *
     * @param args 原始的命令行参数列表
     * @return 解析得到的[CommandLineArgs]
     */
    @JvmStatic
    fun parse(vararg args: String): CommandLineArgs {
        val commandLineArgs = CommandLineArgs()
        for (arg in args) {
            // 如果以--开头的, 切割成为k=v的形式, 并加入到optionArgs当中
            if (arg.startsWith("--")) {
                val keyValue = arg.substring(2)
                val index = keyValue.indexOf("=")
                if (index != -1) {
                    // 切割成为k-v的形式
                    commandLineArgs.addOptionArg(keyValue.substring(0, index), keyValue.substring(index + 1))
                } else {
                    if (keyValue.isEmpty()) {
                        throw IllegalArgumentException("没有找到正确的命令行参数")
                    }
                    commandLineArgs.addOptionArg(keyValue, null)
                }
                // 如果不是以--开头的, 那么直接加入nonArg列表当中
            } else {
                commandLineArgs.addNonArg(arg)
            }
        }
        return commandLineArgs
    }
}