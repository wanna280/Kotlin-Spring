package com.wanna.middleware.arthas.core.shell.command.impl

import com.wanna.middleware.arthas.core.shell.command.AnnotatedCommand
import com.wanna.middleware.arthas.core.shell.command.Command
import com.wanna.middleware.arthas.core.shell.command.CommandProcess
import com.wanna.middleware.arthas.core.shell.handlers.Handler
import com.wanna.middleware.cli.annotation.CLIConfigurator

/**
 * 基于注解的Command的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/26
 */
open class AnnotatedCommandImpl(private val clazz: Class<out AnnotatedCommand>) : Command() {

    private val processHandler = ProcessHandler()

    override fun processHandler(): Handler<CommandProcess> {
        return this.processHandler
    }

    private fun process(process: CommandProcess) {
        val instance: AnnotatedCommand
        try {
            instance = clazz.getDeclaredConstructor().newInstance()
        } catch (ex: Exception) {
            process.end()
            return
        }

        // 执行Setter注入
        CLIConfigurator.inject(process.commandLine(), instance)
        instance.process(process)
    }

    private inner class ProcessHandler : Handler<CommandProcess> {
        override fun handle(event: CommandProcess) {
            process(event)
        }
    }
}