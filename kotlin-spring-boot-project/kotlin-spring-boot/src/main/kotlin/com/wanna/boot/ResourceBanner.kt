package com.wanna.boot

import com.wanna.framework.core.environment.Environment
import com.wanna.framework.core.io.Resource
import java.io.PrintStream

/**
 * 基于资源文件的Banner
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/21
 *
 * @param resource 需要去进行输出的资源
 */
open class ResourceBanner(private val resource: Resource) : Banner {
    override fun printBanner(environment: Environment, sourceClass: Class<*>?, printStream: PrintStream) {
        val stream = resource.getInputStream()
        stream.use { printStream.println(String(stream.readAllBytes())) }
    }
}