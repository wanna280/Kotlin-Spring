package com.wanna.spring.shell.command

import com.wanna.framework.context.annotation.Autowired
import com.wanna.spring.shell.CommandRegistry
import com.wanna.spring.shell.annotation.ShellComponent
import com.wanna.spring.shell.annotation.ShellMethod

/**
 * 列举出Shell帮助信息
 */
@ShellComponent
class Help {

    // 注入Command的注册中心
    @Autowired
    private lateinit var commandRegistry: CommandRegistry

    @ShellMethod(value = "列举出当前ShellApplication支持的所有命令列表")
    fun help(): String {
        return listCommands()
    }

    private fun listCommands(): String {
        val builder = StringBuilder()
        commandRegistry.listCommands()
            .forEach {
                builder.append("\t\t\t\t").append(it.key).append(":   ").append(it.value.help.description).append("\n")
            }
        return builder.toString()
    }
}