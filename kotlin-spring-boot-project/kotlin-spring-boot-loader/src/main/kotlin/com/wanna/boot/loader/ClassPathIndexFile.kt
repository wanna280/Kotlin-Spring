package com.wanna.boot.loader

import java.io.*
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL
import java.util.stream.Collectors

/**
 * 维护了ClassPath的索引的文件("classpath.idx")的描述, 维护了一个应用当中依赖的所有的Jar包的列表
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/26
 * @param root RootFile
 * @param _lines 文件当中的行的列表(需要经过转换)
 */
class ClassPathIndexFile(val root: File, _lines: List<String>) {

    /**
     * 对每一行的数据去进行转换
     */
    private val lines: List<String> = _lines.map(this::extractName)

    /**
     * 将ClassPathIndexFile当中的一行去提取成为Jar包的Name
     *
     * @param line 原始的line
     * @return 提取得到的name
     */
    private fun extractName(line: String): String {
        if (line.startsWith("- \"") && line.endsWith("\"")) {
            return line.substring(3, line.length - 1)
        }
        throw IllegalStateException("不合法的ClassPathIndexLine [$line]")
    }

    /**
     * 获取ClassPathIndexFile文件的行数
     *
     * @return 文件的行数
     */
    fun size(): Int = lines.size

    /**
     * 判断当前的ClassPathIndexFile是否包含了给定的Entry? 
     *
     * @param name entryName
     * @return 如果包含的话, 那么return true; 否则return false
     */
    fun containsEntry(name: String): Boolean = lines.contains(name)

    /**
     * 将ClassPathIndexFile当中的每一行去转换成为一个URL
     *
     * @return 将文件当中的每一行结果
     * @throws IllegalStateException 如果转换得到的URL不合法的话
     */
    fun getUrls(): List<URL> = lines.map(this::asUrl)

    /**
     * 将给定的classpath index line去转换成为URL
     *
     * @param line line
     */
    private fun asUrl(line: String): URL {
        try {
            return File(this.root, line).toURI().toURL()
        } catch (ex: MalformedURLException) {
            throw IllegalStateException(ex)
        }
    }

    companion object {
        @JvmStatic
        fun loadIfPossible(root: URL, location: String): ClassPathIndexFile? {
            return loadIfPossible(asFile(root), location)
        }

        @JvmStatic
        @kotlin.jvm.Throws(IOException::class)
        private fun loadIfPossible(root: File, location: String): ClassPathIndexFile? {
            return loadIfPossible(root, File(root, location))
        }

        @JvmStatic
        @kotlin.jvm.Throws(IOException::class)
        private fun loadIfPossible(root: File, indexFile: File): ClassPathIndexFile? {
            if (indexFile.exists() && indexFile.isFile) {
                FileInputStream(indexFile).use { inputStream ->
                    return ClassPathIndexFile(
                        root,
                        loadLines(inputStream)
                    )
                }
            }
            return null
        }

        /**
         * 从给定的输入流当中去按行去进行文件的读取
         *
         * @param inputStream inputStream
         * @return 读取到的行的列表
         */
        @JvmStatic
        private fun loadLines(inputStream: InputStream): List<String> {
            return BufferedReader(InputStreamReader(inputStream)).lines().collect(Collectors.toList())
        }

        /**
         * 将给定的URL去转换成为File
         *
         * @param url URL
         * @return 转换之后的File对象
         */
        @JvmStatic
        private fun asFile(url: URL): File {
            require("file" == url.protocol) { "给定的URL[$url]并不是一个文件的协议, 无法去引用一个文件" }
            return try {
                File(url.toURI())
            } catch (ex: URISyntaxException) {
                File(url.path)
            }
        }
    }

}