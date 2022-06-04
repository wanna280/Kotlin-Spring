package com.wanna.spring.shell

/**
 * 这是一个MethodTarget的注册器，负责完成MethodTarget的注册
 */
interface MethodTargetRegistrar {

    /**
     * 传递给你CommandRegistry，去处理MethodTarget的注册
     *
     * @param commandRegistry Command的注册中心
     */
    fun register(commandRegistry: ConfigurableCommandRegistry)
}