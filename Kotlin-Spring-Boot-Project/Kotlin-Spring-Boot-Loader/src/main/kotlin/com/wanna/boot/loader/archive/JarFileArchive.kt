package com.wanna.boot.loader.archive

import com.wanna.boot.loader.jar.JarFile
import java.io.File
import java.net.URL
import java.util.jar.JarEntry
import java.util.jar.Manifest

/**
 * Jar包格式的Java归档文件，通过内部组合一个JarFile去完成功能的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/26
 * @param jarFile JarFile
 */
open class JarFileArchive(private val jarFile: JarFile) : Archive {

    /**
     * JarFile URL
     */
    private var url: URL? = null

    constructor(file: File) : this(JarFile(file)) {
        this.url = file.toURI().toURL()
    }

    constructor(file: File, url: URL) : this(file) {
        this.url = url
    }

    /**
     * JarEntry的迭代器
     *
     * @return Entry迭代器
     */
    override fun iterator(): Iterator<Archive.Entry> = EntryIterator()

    /**
     * 在关闭的方法当中，需要去关闭JarFile
     */
    override fun close() {
        jarFile.close()
    }

    /**
     * 获取当前JarFile归档文件的URL
     *
     * @return 如果指定了URL那么直接返回URL；如果没有指定URL，那么使用JarFile去获取URL
     */
    override fun getUrl() = url ?: jarFile.getUrl()


    /**
     * 获取当前Jar包的Manifest
     *
     * @return Manifest
     */
    override fun getManifest(): Manifest = jarFile.manifest

    private abstract class AbstractIterator<T> : Iterator<T> {
        override fun hasNext(): Boolean {
            TODO("Not yet implemented")
        }

        override fun next(): T {
            TODO("Not yet implemented")
        }
    }

    private class NestedArchiveIterator : AbstractIterator<Archive>() {

    }

    private class EntryIterator : AbstractIterator<Archive.Entry>() {

    }

    /**
     * 将Java当中的JarEntry桥接到Archive.Entry当中
     *
     * @param jarEntry JarEntry
     */
    private class JarFileEntry(val jarEntry: JarEntry) : Archive.Entry {
        override fun isDirectory(): Boolean = jarEntry.isDirectory
        override fun getName(): String = jarEntry.name
    }
}