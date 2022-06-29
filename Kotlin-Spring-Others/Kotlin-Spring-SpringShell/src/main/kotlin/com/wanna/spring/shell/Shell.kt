package com.wanna.spring.shell

import com.wanna.framework.beans.SmartInitializingSingleton
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.core.convert.support.DefaultConversionService
import com.wanna.framework.core.util.ReflectionUtils
import com.wanna.spring.shell.result.ResultHandler
import java.lang.reflect.UndeclaredThrowableException

open class Shell(private val resultHandler: ResultHandler<Any>) : CommandRegistry, SmartInitializingSingleton {

    companion object {
        private val EMPTY_INPUT = Any()  // 判断用户为空输入的标识符
    }

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    private val commandMap = LinkedHashMap<String, MethodTarget>()

    private val conversionService = DefaultConversionService()

    override fun listCommands(): Map<String, MethodTarget> = commandMap

    /**
     * 完成初始化所有的Command的初始化工作
     *
     * Note: 这里不能过早完成初始化，因为有可能MethodTargetRegistrar还没完成Bean的注册
     */
    override fun afterSingletonsInstantiated() {
        val commandRegistry = DefaultCommandRegistry()
        applicationContext.getBeanNamesForType(MethodTargetRegistrar::class.java, true, true).forEach {
            val registrar = applicationContext.getBean(it, MethodTargetRegistrar::class.java)
            registrar.register(commandRegistry)
        }
        this.commandMap.putAll(commandRegistry.listCommands())
    }

    open fun run(provider: InputProvider) {
        while (true) {
            var input: Input?
            try {
                input = provider.readInput()  // 接收用户的输入情况
            } catch (ex: Exception) {
                if (ex is ExitRequest) {  // 处理用户在输入的时候按下了Ctrl+C的情况
                    break
                }
                resultHandler.handleResult(ex)  // 处理结果，并且直接忽略掉
                continue
            }
            val result = evaluate(input)
            if (result is ExitRequest) {
                break
            }
            if (result == EMPTY_INPUT || result == null) {
                continue
            }
            resultHandler.handleResult(result)
        }
    }

    open fun evaluate(input: Input): Any? {
        val commands = input.words()
        if (commands.isEmpty()) {
            return EMPTY_INPUT
        }
        val command = commands[0]
        // 如果获取不到命令，那么说明command not found
        val target = listCommands()[command] ?: return CommandNotFound(commands)

        // 执行目标方法，并处理异常情况
        return try {
            // 首先需要去解析目标ShellyMethod当中的参数列表(比如"add 1 2"，需要解析的就是"1 2"部分的参数)
            val parameterTypes = target.method.parameterTypes
            val argsToUse = arrayOfNulls<Any>(commands.size - 1)  // 申请commands.size-1的长度
            argsToUse.indices.forEach { index ->
                if (conversionService.canConvert(String::class.java, parameterTypes[index])) {
                    argsToUse[index] = conversionService.convert(commands[index + 1], parameterTypes[index])
                }
            }
            // 反射执行目标方法
            ReflectionUtils.invokeMethod(target.method, target.bean, *argsToUse)
        } catch (ex: UndeclaredThrowableException) {
            ex.cause
        } catch (ex: Exception) {
            ex
        }
    }

    /**
     * 判断用户的输入是否是空的输入
     *
     * @param input 用户的输入情况
     * @return 是否是空的输入
     */
    private fun noInput(input: Input): Boolean {
        return input.words().isEmpty() || (input.words().size == 1 && input.words()[0].trim().isEmpty())
    }
}