package com.wanna.boot

import com.wanna.framework.core.environment.Environment
import java.io.PrintStream

/**
 * 它负责完成SpringApplication的Banner的打印
 */
interface Banner {
    fun printBanner(environment: Environment, sourceClass: Class<*>, printStream: PrintStream)

    /**
     * 打印的模式枚举，包括No/Console/Log三种方式
     */
    enum class Mode {
        NO, CONSOLE, LOG;
    }
}