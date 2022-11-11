package com.wanna.spring.shell.command

import com.wanna.spring.shell.ExitRequest
import com.wanna.spring.shell.annotation.ShellComponent
import com.wanna.spring.shell.annotation.ShellMethod

/**
 * 退出命令
 */
@ShellComponent
class Quit {

    /**
     * 支持去处理quit和exit命令，直接抛出ExistRequest异常退出应用程序
     *
     * @throws ExitRequest
     */
    @ShellMethod(key = ["quit", "exit"], value = "退出当前ShellApplication")
    fun quit() {
        throw ExitRequest()
    }
}