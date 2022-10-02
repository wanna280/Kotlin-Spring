package com.wanna.boot.loader

/**
 *
 * 用于完成Jar包的启动的启动器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/26
 */
open class JarLauncher() : ExecutableArchiveLauncher() {
    override fun getArchiveEntryPathPrefix() = "BOOT-INF/"

    companion object {

        /**
         * Main方法，用于启动整个应用
         *
         * @param args 命令行参数列表
         */
        @JvmStatic
        fun main(args: Array<String>) {
            JarLauncher().launch(args)
        }
    }
}