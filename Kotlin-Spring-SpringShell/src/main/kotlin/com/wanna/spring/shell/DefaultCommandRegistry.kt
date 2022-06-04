package com.wanna.spring.shell

/**
 * 默认的CommandRegistry的实现
 *
 * @see CommandRegistry
 * @see ConfigurableCommandRegistry
 */
open class DefaultCommandRegistry : ConfigurableCommandRegistry {

    private val commandMap = LinkedHashMap<String, MethodTarget>()

    override fun listCommands(): Map<String, MethodTarget> = commandMap

    override fun register(name: String, command: MethodTarget) {
        val oldCommand = commandMap[name]
        if (oldCommand != null) {
            throw IllegalStateException("之前已经注册过当前的command，不能重复注册[command=$name]")
        }
        commandMap[name] = command
    }
}