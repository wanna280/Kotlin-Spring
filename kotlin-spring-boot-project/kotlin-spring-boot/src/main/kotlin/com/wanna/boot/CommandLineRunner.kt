package com.wanna.boot

/**
 * 这是一个命令行的Runner, 在Spring应用启动完成之后, 会自动回调, 和ApplicationRunner等效
 *
 * @see ApplicationRunner
 */
interface CommandLineRunner {
    fun run(args: Array<String>)
}