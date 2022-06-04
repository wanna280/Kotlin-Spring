package com.wanna.spring.shell

/**
 * Command的注册中心
 *
 * @see ConfigurableCommandRegistry
 * @see DefaultCommandRegistry
 * @see Shell
 */
interface CommandRegistry {

    /**
     * 列举出来所有的Command列表
     *
     * @return Command列表(key-commandName, value-该命令对应的Handler)
     */
    fun listCommands(): Map<String, MethodTarget>
}