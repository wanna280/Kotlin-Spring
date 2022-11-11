package com.wanna.spring.shell

/**
 * 这是一个支持了Command的配置(提供了Command的注册功能)的Command注册中心
 *
 * @see CommandRegistry
 */
interface ConfigurableCommandRegistry : CommandRegistry {

    /**
     * 注册一个Command以及它的处理方法到注册中心当中
     *
     * @param name 要接收的命令(args[0])
     * @param command 处理该命令的目标方法
     */
    fun register(name: String, command: MethodTarget)
}