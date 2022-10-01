package com.wanna.framework.util

import java.net.URI
import java.net.URL
import java.util.*

/**
 * 提供资源相关操作的工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/1
 */
object ResourceUtils {

    /**
     * 文件夹的分隔符
     */
    const val FOLDER_SEPARATOR = StringUtils.FOLDER_SEPARATOR

    /**
     * 类路径(ClassPath)的前缀
     */
    const val CLASSPATH_URL_PREFIX = "classpath:"

    /**
     * 类型为"file"的URL协议
     */
    const val URL_PROTOCOL_FILE = "file"

    @JvmStatic
    fun toURI(url: URL): URI = toURI(url.toString())

    @JvmStatic
    fun toURI(uri: String): URI = URI(uri)

    /**
     *  根据path和relativePath去得到一个完成的路径
     *
     *  @param path path
     *  @param relativePath 相对path的相对路径
     *  @return 转换得到的绝对路径
     */
    @JvmStatic
    fun applyRelativePath(path: String, relativePath: String): String {
        StringUtils
        val separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR)
        return if (separatorIndex != -1) {
            var newPath = path.substring(0, separatorIndex)
            if (!relativePath.startsWith(FOLDER_SEPARATOR)) {
                newPath += FOLDER_SEPARATOR
            }
            newPath + relativePath
        } else {
            relativePath
        }
    }

    /**
     * 判断给定的URL是否是一个文件的URL
     */
    @JvmStatic
    fun isFileURL(url: URL): Boolean {
        return Objects.equals(url.protocol, URL_PROTOCOL_FILE)
    }
}