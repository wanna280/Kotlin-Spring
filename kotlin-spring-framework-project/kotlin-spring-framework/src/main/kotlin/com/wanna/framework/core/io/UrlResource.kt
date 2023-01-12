package com.wanna.framework.core.io

import com.wanna.framework.lang.Nullable
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
 */
open class UrlResource(private var url: URL?, private var uri: URI?) : AbstractFileResolvingResource() {
    constructor(url: URL?) : this(url, null)

    constructor(uri: URI?) : this(uri?.toURL(), uri)

    /**
     * 给定一个path去构建URL并构建出来URLResource
     *
     * @param path path
     * @throws MalformedURLException 如果给定的path不合法
     */
    @Throws(MalformedURLException::class)
    constructor(path: String) : this(URL(path))

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

    override fun getDescription() = "URL [ $url ]"

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
     * 父类没有实现获取URL逻辑，我们这里需要提供
     *
     * @return URL
     * @throws IllegalStateException 如果URL为空
     */
    override fun getURL() = this.url ?: throw IllegalStateException("URL不能为空")

    /**
     * 重写父类逻辑，优先使用子类的URI作为URL，子类没有再沿用父类的逻辑
     *
     * @return URI
     */
    override fun getURI() = if (this.uri != null) this.uri!! else super.getURI()
}