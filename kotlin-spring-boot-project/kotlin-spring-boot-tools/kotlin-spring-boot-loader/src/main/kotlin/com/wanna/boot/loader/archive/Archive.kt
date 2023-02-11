package com.wanna.boot.loader.archive

import com.wanna.boot.loader.Launcher
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
import java.util.jar.Manifest

/**
 * 用于描述一个Java的归档文件(比如Jar包/Zip包), 它能支持被Launcher去进行引导和启动
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/26
 *
 * @see Launcher
 * @see JarFileArchive
 * @see ExplodedArchive
 */
interface Archive : Iterable<Archive.Entry> {

    /**
     * 获取当前归档文件的URL
     *
     * @return 当前归档文件的URL
     * @throws MalformedURLException 如果当前的Jar包不合法(malformed-畸形的)
     */
    @Throws(MalformedURLException::class)
    fun getUrl(): URL

    /**
     * 获取该归档文件的Manifest(显现)
     *
     * @return Manifest
     * @throws IOException 如果找不到Manifest文件的话
     */
    @Throws(IOException::class)
    fun getManifest(): Manifest?

    /**
     * 当前的归档文件是否已经被解压(unpack-解压)了? 默认为false
     *
     * @return 如果已经被解压, 那么return true; 如果没有被解压, 那么return false
     */
    fun isExploded(): Boolean = false

    /**
     * 如果必要的话, 需要去释放当前归档文件相关的资源
     */
    fun close() {
        // do something...
    }

    /**
     * 获取当前的归档文件内部嵌套的归档文件列表
     *
     * @param searchFilter 用于搜索的Filter
     * @param includeFilter 需要去进行包含的Filter
     * @return 从当前归档文件内部搜索到的Archive归档文件列表
     */
    fun getNestedArchives(searchFilter: EntryFilter, includeFilter: EntryFilter): Iterator<Archive>

    /**
     * 描述的是Jar/War包当中的一个文件/文件夹, 称为一个Entry, 也就是对应的JarFile当中的JarEntry
     */
    interface Entry {

        /**
         * 判断当前的Entry是否是一个目录?
         */
        val isDirectory: Boolean

        /**
         * 当前Entry的name
         */
        val name: String
    }

    /**
     * 用于去过滤Archive的Entry的Filter
     */
    fun interface EntryFilter {

        /**
         * 对ArchiveEntry去进行过滤
         *
         * @param entry ArchiveEntry
         * @return 如果当前Entry匹配的话, 那么return true; 否则return false
         */
        fun matches(entry: Entry): Boolean
    }
}