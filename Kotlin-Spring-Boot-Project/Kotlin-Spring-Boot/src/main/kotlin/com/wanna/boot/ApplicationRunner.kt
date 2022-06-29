package com.wanna.boot

/**
 * 这是一个SpringApplication的ApplicationRunner，在Spring应用启动完成时，会自动回调；
 * 作用和CommandLineRunner等效
 *
 * @see CommandLineRunner
 */
interface ApplicationRunner {
    fun run(applicationArguments: ApplicationArguments)
}