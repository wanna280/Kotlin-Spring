package com.wanna.middleware.arthas.core.shell

import com.wanna.middleware.arthas.core.shell.command.CommandResolver
import com.wanna.middleware.arthas.core.shell.handlers.Handler
import com.wanna.middleware.arthas.core.shell.handlers.NoOpHandler
import com.wanna.middleware.arthas.core.shell.term.Term
import com.wanna.middleware.arthas.core.shell.term.TermServer
import com.wanna.middleware.arthas.core.shell.impl.ShellServerImpl
import java.util.concurrent.Future

/**
 * ShellServer, 负责去处理Shell命令
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/25
 */
abstract class ShellServer {

    abstract fun createShell(term: Term): Shell

    abstract fun createShell(): Shell

    abstract fun registerCommandResolver(resolver: CommandResolver)

    abstract fun registerTermServer(termServer: TermServer): ShellServer

    @Suppress("UNCHECKED_CAST")
    open fun close() = close(NoOpHandler() as Handler<Future<Void>>)

    abstract fun close(completionHandler: Handler<Future<Void>>)

    @Suppress("UNCHECKED_CAST")
    open fun listen(): ShellServer = listen(NoOpHandler() as Handler<Future<Void>>)

    abstract fun listen(listenHandler: Handler<Future<Void>>): ShellServer


    companion object {

        /**
         * 使用默认的参数, 去启动ShellServer
         *
         * @return ShellServer
         */
        @JvmStatic
        fun create(): ShellServer {
            return ShellServerImpl()
        }

        /**
         * 使用自定义的参数, 去启动ShellServer
         *
         * @return ShellServer
         */
        @JvmStatic
        fun create(options: ShellServerOptions): ShellServer {
            return ShellServerImpl(options)
        }
    }

}