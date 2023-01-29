package com.wanna.framework.core.io

import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ResourceUtils
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URI
import java.net.URL

/**
 * 基于Url的Resource
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/1
 *
 * @see URI
 * @see URL
 */
open class UrlResource(
    @Nullable private var url: URL?,
    @Nullable private var uri: URI?
) : AbstractFileResolvingResource() {

    /**
     * 提供一个基于[URL]去构建[UrlResource]的构造器
     *
     * @param url URL
     */
    constructor(@Nullable url: URL?) : this(url, null)

    /**
     * 提供一个基于[URI]去构建[UrlResource]的构造器
     *
     * @param uri URI
     */
    constructor(@Nullable uri: URI?) : this(uri?.toURL(), uri)

    /**
     * 给定一个path去构建URL并构建出来URLResource
     *
     * @param path path
     * @throws MalformedURLException 如果给定的path不合法
     */
    @Throws(MalformedURLException::class)
    constructor(path: String) : this(ResourceUtils.toURL(path))

    /**
     * 根据protocol和path去构建[UrlResource]
     *
     * @param protocol protocol
     * @param path path
     */
    constructor(protocol: String, path: String) : this(protocol, path, null)

    /**
     * 根据protocol/path/fragment去构建出来URL从而构建出来URLResource
     *
     * @param path path
     * @param protocol protocol
     * @param fragment fragment
     * @throws MalformedURLException 如果给定的path不合法
     */
    @Throws(MalformedURLException::class)
    constructor(protocol: String, path: String, @Nullable fragment: String?) : this(
        URI(protocol, path, fragment).toURL(),  // 不能直接使用URL构造器, 会有NPE
        URI(protocol, path, fragment)
    )

    /**
     * 获取到URLResource的描述信息
     *
     * @return description
     */
    override fun getDescription() = "URL [ $url ]"

    /**
     * 获取到当前[UrlResource]的输入流
     *
     * @return InputStream of this UrlResource
     */
    override fun getInputStream(): InputStream {
        val connection = getURL().openConnection()
        try {
            return connection.getInputStream()
        } catch (ex: IOException) {
            if (connection is HttpURLConnection) {
                connection.disconnect()
            }
            throw ex
        }
    }

    /**
     * 创建一个基于当前Resource的相对路径下的资源
     *
     * @param relativePath 相对当前资源的路径
     * @return 根据相对路径解析到的资源
     * @throws IOException 如果根据该相对路径无法解析到资源的话
     */
    override fun createRelative(relativePath: String): Resource {
        return UrlResource(createRelativeURL(relativePath))
    }

    /**
     * 基于当前Resource的URL, 创建一个相对路径的URL
     *
     * @param relativePath 相对路径
     * @return 解析得到的相对于当前的URL
     */
    protected open fun createRelativeURL(relativePath: String): URL {
        var relativePathToUse = relativePath
        if (relativePathToUse.startsWith("/")) {
            relativePathToUse = relativePathToUse.substring(1)
        }
        return ResourceUtils.createRelative(this.getURL(), relativePathToUse)
    }

    /**
     * 父类没有实现获取URL逻辑, 我们这里需要提供
     *
     * @return URL
     * @throws IllegalStateException 如果URL为空
     */
    override fun getURL() = this.url ?: throw IllegalStateException("URL cannot be null")

    /**
     * 重写父类逻辑, 优先使用子类的URI作为URL, 子类没有再沿用父类的逻辑
     *
     * @return URI
     */
    override fun getURI() = if (this.uri != null) this.uri!! else super.getURI()
}