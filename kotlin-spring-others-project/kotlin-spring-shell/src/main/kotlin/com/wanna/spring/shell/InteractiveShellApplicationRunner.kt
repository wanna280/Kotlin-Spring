package com.wanna.spring.shell

import com.wanna.boot.ApplicationArguments
import com.wanna.boot.ApplicationRunner
import com.wanna.framework.context.annotation.Autowired
import java.util.Scanner

/**
 * 这是一个交互式的Shell的Runner, 在SpringApplication启动完成时自动回调
 *
 * @see Shell
 * @see ApplicationRunner
 */
open class InteractiveShellApplicationRunner : ApplicationRunner {

    @Autowired
    private lateinit var shell: Shell

    override fun run(applicationArguments: ApplicationArguments) {
        val inputProvider: InputProvider = JLineInputProvider()
        shell.run(inputProvider)
    }

    open class JLineInputProvider : InputProvider {
        private val scanner = Scanner(System.`in`)

        override fun readInput(): Input {
            print("shell:>")
            val line = scanner.nextLine()
            return ParsedLineInput(line)
        }
    }
}