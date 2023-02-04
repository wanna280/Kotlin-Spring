package com.wanna.debugger.bistoury.instrument.client.classpath

import java.io.File

/**
 * Web环境下的ClassPath的Supplier
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/4
 *
 * @param appLibDirectory 应用程序的Jar包所在目录
 * @param jarLibPath SpringBoot的Jar包所在目录
 * @param classesPath SpringBoot的项目字节码目录
 */
open class WebAppClassPathSupplier(
    appLibDirectory: String,
    jarLibPath: String,
    classesPath: String
) : AppClassPathSupplier {

    private val classPath: List<String>

    init {
        val appLibDirectoryFile = File(appLibDirectory)
        val appLibDir = appLibDirectoryFile.absolutePath
        val webRoot = appLibDirectoryFile.parent

        // /lib/xxx.jar
        // /classes/...
        // appLibDirectory是lib目录, 在计算classes目录的话, 需要返回上一级目录
        val classesDir = File(webRoot, "classes").absolutePath
        classPath = listOf(classesDir, appLibDir, jarLibPath, classesPath)
    }

    override fun get(): List<String> = this.classPath
}