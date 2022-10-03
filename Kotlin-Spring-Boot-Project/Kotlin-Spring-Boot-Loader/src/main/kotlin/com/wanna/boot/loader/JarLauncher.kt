package com.wanna.boot.loader

import com.wanna.boot.loader.archive.Archive

/**
 *
 * 用于完成Jar包的启动的启动器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/26
 */
open class JarLauncher : ExecutableArchiveLauncher() {

    /**
     * 获取用于去寻找内部的ArchiveEntry的路径前缀
     *
     * @return ArchiveEntry所在路径的前缀
     */
    override fun getArchiveEntryPathPrefix() = "BOOT-INF/"

    /**
     * 如果是文件夹，只要"BOOT-INF/classes"去作为一个归档；
     * 如果不是文件夹，要的是"BOOT-INF/lib/"下的(目录本身不要)
     *
     * @param entry 待匹配的ArchiveEntry
     * @return 它是否是一个合格的嵌套Archive？
     */
    override fun isNestedArchive(entry: Archive.Entry) =
        if (entry.isDirectory()) entry.getName() == "BOOT-INF/classes/" else entry.getName().startsWith("BOOT-INF/lib/")

    companion object {
        /**
         * Main方法，用于启动整个Jar包的应用，它会被Java应用自动回调到
         *
         * @param args 命令行参数列表
         */
        @JvmStatic
        fun main(args: Array<String>) = JarLauncher().launch(args)
    }
}