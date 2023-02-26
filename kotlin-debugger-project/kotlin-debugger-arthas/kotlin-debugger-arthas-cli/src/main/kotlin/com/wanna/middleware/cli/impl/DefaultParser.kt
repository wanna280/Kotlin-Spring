package com.wanna.middleware.cli.impl

import com.wanna.middleware.cli.CLI
import com.wanna.middleware.cli.CommandLine
import com.wanna.middleware.cli.CommandLines

/**
 * 命令行参数的解析器, 将字符串的参数列表, 去解析成为一个[CommandLine]对象
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/26
 */
class DefaultParser {

    var token: String? = null

    /**
     * 是否大小写敏感? 为true代表区分大小写, 为false代表不区分大小写
     */
    var caseSensitive: Boolean = true

    /**
     * 将给定的命令行参数列表, 去解析成为[CommandLine]对象
     *
     * @param cli CLI
     * @param commandLineArgs 命令行参数列表
     * @param validate 是否需要进行参数检验?
     */
    fun parse(cli: CLI, commandLineArgs: List<String>, validate: Boolean): CommandLine {
        val commandLine = CommandLines.create(cli)

        // 为所有的Argument, 去填充index
        var current = 0
        for (argument in cli.getArguments()) {
            if (argument.getIndex() == -1) {
                argument.setIndex(current)
                current++
            } else {
                current = argument.getIndex() + 1
            }
        }
        // sort arguments
        cli.setArguments(cli.getArguments().sortedWith(ArgumentComparator))

        // check Option
        for (option in cli.getOptions()) {
            option.ensureValidity()
        }

        // check Argument
        for (argument in cli.getArguments()) {
            argument.ensureValidity()
        }

        // 解析命令行参数列表, 尝试对所有的参数去进行解析
        for (token in commandLineArgs) {
            visit(token)
        }



        return commandLine
    }

    /**
     * 对单个Token字符串去进行解析
     *
     * @param token
     */
    private fun visit(token: String) {
        this.token = token


    }

}