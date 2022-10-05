package com.wanna.boot.loader

import com.wanna.boot.loader.archive.Archive

/**
 * 用于去完成War包的启动的启动器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/26
 */
open class WarLauncher : ExecutableArchiveLauncher() {

    /**
     * 获取用于去寻找内部的ArchiveEntry的路径前缀
     *
     * @return ArchiveEntry所在路径的前缀
     */
    override fun getArchiveEntryPathPrefix() = "WEB-INF/"

    /**
     * 如果是文件夹，只要"WEB-INF/classes"去作为一个归档；
     * 如果不是文件夹，要的是"WEB-INF/lib/"和"WEB-INF/lib-provided/"下的(目录本身不要)
     *
     * @param entry 待匹配的ArchiveEntry
     * @return 它是否是一个合格的嵌套Archive？
     */
    override fun isNestedArchive(entry: Archive.Entry) =
        if (entry.isDirectory()) entry.getName() == "WEB-INF/classes/" else
            entry.getName().startsWith("WEB-INF/lib/") || entry.getName().startsWith("WEB-INF/lib-provided/")

    companion object {
        /**
         * Main方法，用于启动整个War包的应用，它会被Java应用自动回调到
         *
         * @param args 命令行参数列表
         */
        @JvmStatic
        @Throws(Exception::class)
        fun main(args: Array<String>) = WarLauncher().launch(args)
    }
}