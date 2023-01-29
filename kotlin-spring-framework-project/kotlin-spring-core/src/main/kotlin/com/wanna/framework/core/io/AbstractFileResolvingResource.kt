package com.wanna.framework.core.io

import com.wanna.framework.util.ResourceUtils
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URLConnection

/**
 * 为[Resource]提供对于文件相关的解析功能
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/1
 *
 * @see AbstractResource
 */
abstract class AbstractFileResolvingResource : AbstractResource() {

    /**
     * 重写exists判断[Resource]是否存在的方法, 更新的exists的判断,
     * 主要新增基于[URLConnection]的长度相关的检验去提供判断.
     *
     * @return 如果当前的Resource存在, return true; 否则return false
     */
    override fun exists(): Boolean {
        try {
            val url = getURL()

            // 如果是一个文件URL, 那么获取file去进行判断
            if (ResourceUtils.isFileURL(url)) {
                return getFile().exists()
            }

            val connection = url.openConnection()

            // 给子类一个机会, 让它去对URLConnection去进行自定义...
            customizeConnection(connection)

            // 如果是HTTP的URLConnection的话, 那么检查一下responseCode是否为OK?
            val httpConnection = if (connection is HttpURLConnection) connection else null
            if (httpConnection != null) {
                httpConnection.requestMethod = "HEAD"
                if (httpConnection.responseCode == HttpURLConnection.HTTP_OK) {
                    return true
                } else if (httpConnection.responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    return false
                }
            }

            // 如果contentLength大于0, 那么说明存在
            // 特殊地: 对于"jar:file:/.../b.jar!/"这种没有指定entryName的Jar路径的情况,
            // 走后面的InputStream的判断会失败(no entry name), 但是走这里的contentLength的检查可以快速通过...
            if (connection.contentLengthLong > 0) {
                return true
            }

            if (httpConnection != null) {
                // 如果没有HTTP的Content-Length这个Header, 也没有HTTP的OK状态码, 那么放弃了...return false
                httpConnection.disconnect()
                return false
            } else {
                // fallback, 检查InputStream流是否可以打开?
                getInputStream().close()
                return true
            }

        } catch (ex: IOException) {
            return false
        }
    }

    /**
     * 给子类一个机会, 去执行对于[URLConnection]的自定义
     *
     * @param connection URLConnection
     */
    protected open fun customizeConnection(connection: URLConnection) {
        if (connection is HttpURLConnection) {
            customizeConnection(connection)
        }
    }

    /**
     * 给子类一个机会, 去执行对于[HttpURLConnection]的自定义
     *
     * @param connection HttpURLConnection
     */
    protected open fun customizeConnection(connection: HttpURLConnection) {

    }

    /**
     * 是否是文件? 通过url当中去检查协议是否是"file"的方式去进行检查
     *
     * @return 如果是文件的话, return true; 否则return false
     */
    override fun isFile(): Boolean {
        try {
            return ResourceUtils.isFileURL(getURL())
        } catch (ex: IOException) {
            return false
        }
    }

    /**
     * 获取当前[Resource]对应的文件对象
     *
     * @return File
     */
    override fun getFile(): File = File(getURL().toString())
}