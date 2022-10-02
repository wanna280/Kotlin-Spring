package com.wanna.boot.loader

/**
 * 用于去完成War包的启动的启动器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/26
 */
open class WarLauncher : ExecutableArchiveLauncher() {
    override fun getArchiveEntryPathPrefix() = "WEB-INF/"
}