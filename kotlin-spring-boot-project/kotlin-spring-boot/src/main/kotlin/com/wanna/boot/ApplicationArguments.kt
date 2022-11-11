package com.wanna.boot

/**
 * 这是一个SpringApplication的参数列表，内部维护了命令行参数信息
 *
 * @see CommandLineRunner.run
 * @see ApplicationRunner.run
 */
interface ApplicationArguments {
    /**
     * 获取原始的命令行参数列表
     *
     * @return 原始的命令行参数列表
     */
    fun getSourceArgs(): Array<String>
}