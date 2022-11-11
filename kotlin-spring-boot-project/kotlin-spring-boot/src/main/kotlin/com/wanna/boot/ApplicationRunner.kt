package com.wanna.boot

/**
 * 这是一个SpringApplication的ApplicationRunner，在Spring应用启动完成时，会自动回调；
 * 这个组件的作用和CommandLineRunner等效，区别在于这个给的是解析完成的命令行参数，CommandLineRunner给的是原始的命令行参数
 *
 * @see CommandLineRunner
 */
interface ApplicationRunner {
    fun run(applicationArguments: ApplicationArguments)
}