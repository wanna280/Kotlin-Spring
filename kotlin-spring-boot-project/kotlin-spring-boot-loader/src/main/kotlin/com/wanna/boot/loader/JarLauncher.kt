package com.wanna.boot.loader

import com.wanna.boot.loader.archive.Archive

/**
 * 用于完成Jar包的启动的启动器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/26
 */
open class JarLauncher : ExecutableArchiveLauncher() {

    /**
     * 对于Jar包启动的方式, 需要去进行搜索的归档文件的Entry的前缀为"BOOT-INF/",
     * 只有这样的Entry, 才是我们需要去进行搜索的Entry
     *
     * @return "BOOT-INF/"
     */
    override fun getArchiveEntryPathPrefix() = "BOOT-INF/"

    /**
     * * 1.如果给定的归档文件的Entry是文件夹, 只要"BOOT-INF/classes"去作为一个归档;
     * * 2.如果给定的归档文件的Entry不是文件夹, 要的是"BOOT-INF/lib/"下的Entry(对于"BOOT-INF/lib"这个目录本身不要)
     *
     * @param entry 待匹配的ArchiveEntry
     * @return 它是否是一个合格的嵌套Archive?
     */
    override fun isNestedArchive(entry: Archive.Entry) =
        if (entry.isDirectory()) entry.getName() == "BOOT-INF/classes/" else entry.getName().startsWith("BOOT-INF/lib/")

    companion object {
        /**
         * Main方法, 用于启动整个Jar包的应用, 它会被Java应用自动回调到
         *
         * @param args 命令行参数列表
         */
        @JvmStatic
        @Throws(Exception::class)
        fun main(args: Array<String>) = JarLauncher().launch(args)
    }
}