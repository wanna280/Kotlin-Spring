package com.wanna.middleware.arthas.core.shell.command

import com.wanna.middleware.arthas.core.shell.cli.Completion
import com.wanna.middleware.arthas.core.shell.command.impl.AnnotatedCommandImpl
import com.wanna.middleware.arthas.core.shell.handlers.Handler
import com.wanna.middleware.cli.CLI
import javax.annotation.Nullable

/**
 * Shell命令
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/26
 */
abstract class Command {

    companion object {
        /**
         * 创建[Command]的工厂方法
         *
         * @param clazz 处理注解的命令的类
         * @return Command
         */
        @JvmStatic
        fun create(clazz: Class<out AnnotatedCommand>): Command {
            return AnnotatedCommandImpl(clazz)
        }
    }

    @Nullable
    open fun name(): String? = null

    @Nullable
    open fun cli(): CLI? = null

    abstract fun processHandler(): Handler<CommandProcess>

    open fun complete(completion: Completion) {
        completion.complete(emptyList())
    }
}