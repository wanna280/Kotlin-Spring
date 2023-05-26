package com.wanna.middleware.cli

/**
 * 创建[CommandLine]的工厂方法
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/26
 */
object CommandLines {

    /**
     * 创建[CommandLine]
     *
     * @param cli CLI
     * @return CommandLine
     */
    @JvmStatic
    fun create(cli: CLI): DefaultCommandLine {
        return DefaultCommandLine(cli)
    }
}