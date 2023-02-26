package com.wanna.middleware.cli.impl

import com.wanna.middleware.cli.CLI
import com.wanna.middleware.cli.CommandLine
import com.wanna.middleware.cli.CommandLines

/**
 * 命令行参数的解析器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/26
 */
class DefaultParser {

    /**
     * 是否大小写敏感? 为true代表区分大小写, 为false代表不区分大小写
     */
    var caseSensitive: Boolean = true

    /**
     * 将给定的命令行参数列表, 去解析成为[CommandLine]
     *
     * @param cli CLI
     * @param arguments 命令行参数列表
     * @param validate 是否需要进行参数检验?
     */
    fun parse(cli: CLI, arguments: List<String>, validate: Boolean): CommandLine {
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

        return commandLine
    }

}