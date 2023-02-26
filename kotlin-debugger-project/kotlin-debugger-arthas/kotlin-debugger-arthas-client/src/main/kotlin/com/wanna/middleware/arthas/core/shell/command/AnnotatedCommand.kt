package com.wanna.middleware.arthas.core.shell.command

import com.wanna.middleware.arthas.core.shell.cli.Completion
import com.wanna.middleware.cli.CLI
import javax.annotation.Nullable

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/26
 */
abstract class AnnotatedCommand {

    @Nullable
    open fun name(): String? = null

    @Nullable
    open fun cli(): CLI? = null

    open fun complete(completion: Completion) {
        // TODO
    }

    abstract fun process(process: CommandProcess)
}