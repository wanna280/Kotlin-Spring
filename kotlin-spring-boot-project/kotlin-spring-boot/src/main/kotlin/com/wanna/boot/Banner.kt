package com.wanna.boot

import com.wanna.framework.core.environment.Environment
import java.io.PrintStream

/**
 * 它负责完成SpringApplication的Banner的打印
 *
 * @see Mode
 * @see SpringBootBanner
 */
interface Banner {

    /**
     * 打印Banner
     *
     * @param environment SpringApplication的Environment
     * @param sourceClass 源类
     * @param printStream 输出流
     */
    fun printBanner(environment: Environment, sourceClass: Class<*>?, printStream: PrintStream)

    /**
     * Banner的打印的模式枚举, 包括No/Console/Log三种方式
     */
    enum class Mode {
        NO, CONSOLE, LOG;
    }
}