package com.wanna.middleware.arthas.core.shell.impl

import com.wanna.middleware.arthas.core.server.ArthasBootstrap
import com.wanna.middleware.arthas.core.shell.Shell
import com.wanna.middleware.arthas.core.shell.ShellServer
import com.wanna.middleware.arthas.core.shell.ShellServerOptions
import com.wanna.middleware.arthas.core.shell.command.CommandResolver
import com.wanna.middleware.arthas.core.shell.handlers.Handler
import com.wanna.middleware.arthas.core.shell.term.Term
import com.wanna.middleware.arthas.core.shell.term.TermServer
import java.util.concurrent.Future
import javax.annotation.Nullable

/**
 * ShellServer的具体实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/25
 */
open class ShellServerImpl @JvmOverloads constructor(
    private val options: ShellServerOptions = ShellServerOptions(),
    @Nullable private val arthasBootstrap: ArthasBootstrap? = null
) : ShellServer() {

    override fun createShell(term: Term): Shell {
        TODO("Not yet implemented")
    }

    override fun createShell(): Shell {
        TODO("Not yet implemented")
    }

    override fun registerCommandResolver(resolver: CommandResolver) {
        TODO("Not yet implemented")
    }

    override fun registerTermServer(termServer: TermServer): ShellServer {
        TODO("Not yet implemented")
    }

    override fun close(completionHandler: Handler<Future<Void>>) {
        TODO("Not yet implemented")
    }

    override fun listen(listenHandler: Handler<Future<Void>>): ShellServer {
        TODO("Not yet implemented")
    }
}