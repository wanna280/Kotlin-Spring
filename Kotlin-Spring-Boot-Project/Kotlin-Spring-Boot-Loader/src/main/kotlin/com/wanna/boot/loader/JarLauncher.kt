package com.wanna.boot.loader

/**
 *
 * 用于完成Jar包的启动的启动器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/26
 */
open class JarLauncher : ExecutableArchiveLauncher() {


    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            JarLauncher().launch(args)
        }
    }
}