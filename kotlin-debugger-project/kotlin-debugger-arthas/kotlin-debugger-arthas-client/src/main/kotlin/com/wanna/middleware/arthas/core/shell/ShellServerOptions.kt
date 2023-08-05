package com.wanna.middleware.arthas.core.shell

import java.lang.instrument.Instrumentation
import javax.annotation.Nullable

/**
 * ShellServer的参数信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/25
 */
open class ShellServerOptions {

    @Nullable
    private var instrumentation: Instrumentation? = null

    private var pid: Int = -1

    fun setInstrumentation(instrumentation: Instrumentation): ShellServerOptions {
        this.instrumentation = instrumentation
        return this
    }

    fun setPid(pid: Int): ShellServerOptions {
        this.pid = pid
        return this
    }
}