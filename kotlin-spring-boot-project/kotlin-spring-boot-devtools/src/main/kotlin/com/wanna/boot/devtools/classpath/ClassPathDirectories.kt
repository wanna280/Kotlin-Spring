package com.wanna.boot.devtools.classpath

import java.io.File
import java.net.URL

/**
 * 用于将URL转换成为一个目录(File & File.isDirectory)
 * 将ClassPath的URL去进行过滤, 获取到所有的本地的输出的类路径, 并包装成为File
 *
 * @param urls 候选的去进行匹配的URL列表
 */
class ClassPathDirectories(private val urls: Array<URL>) : Iterable<File> {

    /**
     * 将给定的URL当中的所有的文件夹过滤出来, 并转为File
     *
     * @return Files
     */
    override fun iterator() = this.urls
        .filter { it.protocol == "file" && it.path.endsWith("/") }
        .map { File(it.path) }
        .iterator()
}