package com.wanna.boot

import com.wanna.framework.core.environment.Environment
import java.io.PrintStream

/**
 * 资源的Banner
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/21
 */
open class ResourceBanner(val url: String) : Banner {
    override fun printBanner(environment: Environment, sourceClass: Class<*>?, printStream: PrintStream) {
        val stream = ClassLoader.getSystemClassLoader().getResourceAsStream(url)
        stream ?: return
        stream.use { printStream.println(String(stream.readAllBytes())) }
    }
}