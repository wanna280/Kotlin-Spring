package com.wanna.middleware.arthas.core.shell.command

import com.wanna.middleware.arthas.core.shell.term.Tty
import com.wanna.middleware.cli.CommandLine


/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/26
 */
interface CommandProcess : Tty {

    fun commandLine(): CommandLine

    fun end()
}