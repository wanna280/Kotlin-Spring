package com.wanna.framework.core.io

import com.wanna.framework.lang.Nullable
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URL
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel

/**
 * Spring家对于资源的抽象
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/1
 */
interface Resource : InputStreamSource {

    /**
     * 判断给资源是否存在?
     *
     * @return 如果该资源存在的话, return true; 不存在则return false
     */
    fun exists(): Boolean

    /**
     * 该资源是否是可读的? 默认情况的实现下, 只要该资源存在就是可读的
     *
     * @return 如果该文件可读, 那么return true; 否则return false
     */
    fun isReadable(): Boolean = exists()

    /**
     * 该资源当前是否是打开的?
     *
     * @return 如果该资源已经打开了, 那么return true; 否则return false
     */
    fun isOpen(): Boolean = false

    /**
     * 当前资源是否是一个文件?
     *
     * @return 如果当前资源是一个文件的话, return true; 否则return false
     */
    fun isFile(): Boolean = false

    /**
     * 获取资源的URI
     *
     * @return 资源URI
     * @throws IOException 如果无法将该资源去解析成为URI
     */
    @Throws(IOException::class)
    fun getURI(): URI

    /**
     * 获取资源的URL
     *
     * @return 资源URL
     * @throws IOException 如果无法将该资源去解析成为URL
     */
    @Throws(IOException::class)
    fun getURL(): URL

    /**
     * 获取当前资源的文件
     *
     * @return 当前资源的文件
     * @throws IOException 如果当前资源无法被解析成为文件
     */
    @Throws(IOException::class)
    fun getFile(): File

    /**
     * 获取当前资源的一个可读的Channel
     *
     * @return 当前资源的一个可读的Channel
     * @throws IOException 如果Channel无法打开
     */
    @Throws(IOException::class)
    fun readableChannel(): ReadableByteChannel = Channels.newChannel(getInputStream())

    /**
     * 获取当前资源上次被修改的时间戳
     *
     * @return 上次修改的时间戳
     * @throws IOException 如果该资源无法被解析
     */
    @Throws(IOException::class)
    fun lastModified(): Long

    /**
     * 获取该资源的内容的长度
     *
     * @return 资源内容长度
     * @throws IOException 如果该资源无法被解析
     */
    @Throws(IOException::class)
    fun contentLength(): Long

    /**
     * 创建一个基于当前Resource的相对路径下的资源
     *
     * @param relativePath 相对当前资源的路径
     * @return 根据相对路径解析到的资源
     * @throws IOException 如果根据该相对路径无法解析到资源的话
     */
    @Throws(IOException::class)
    fun createRelative(relativePath: String): Resource

    /**
     * 获取该资源的文件名
     *
     * @return 资源文件名(如果该资源无文件名return null)
     */
    @Nullable
    fun getFilename(): String?

    /**
     * 获取该资源的描述信息
     *
     * @return 资源的描述信息(可以为null)
     */
    @Nullable
    fun getDescription(): String?
}