package com.wanna.spring.shell.annotation

import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.processor.beans.BeanPostProcessor
import com.wanna.framework.core.util.ReflectionUtils
import com.wanna.spring.shell.Command
import com.wanna.spring.shell.ConfigurableCommandRegistry
import com.wanna.spring.shell.MethodTarget
import com.wanna.spring.shell.MethodTargetRegistrar
import org.springframework.core.annotation.AnnotatedElementUtils

/**
 * ShellCommand的MethodTarget的注册器
 *
 * @see MethodTargetRegistrar
 */
open class ShellCommandMethodTargetRegistrar : MethodTargetRegistrar, BeanPostProcessor {

    private val shellMethods = LinkedHashMap<String, MethodTarget>()

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    override fun register(commandRegistry: ConfigurableCommandRegistry) {
        shellMethods.forEach(commandRegistry::register)
    }

    override fun postProcessAfterInitialization(beanName: String, bean: Any): Any? {
        val beanType = bean::class.java
        if (AnnotatedElementUtils.isAnnotated(beanType, ShellComponent::class.java)) {
            ReflectionUtils.doWithMethods(beanType) {
                if (AnnotatedElementUtils.isAnnotated(it, ShellMethod::class.java)) {
                    val shellMethod = AnnotatedElementUtils.getMergedAnnotation(it, ShellMethod::class.java)!!
                    var commandNames = shellMethod.key
                    if (commandNames.isEmpty()) {
                        commandNames = arrayOf(it.name)
                    }

                    // 注册ShellMethod
                    commandNames.forEach { command ->
                        shellMethods[command] = MethodTarget(it, bean, Command.Help(shellMethod.value))
                    }
                }
            }
        }
        return bean
    }
}