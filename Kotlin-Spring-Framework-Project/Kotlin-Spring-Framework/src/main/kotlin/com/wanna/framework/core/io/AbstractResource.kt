package com.wanna.framework.core.io

import com.wanna.framework.util.ResourceUtils
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.URISyntaxException
import java.net.URL

/**
 * 抽象的Resource的实现，实现了一些Resource的典型行为
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/1
 */
abstract class AbstractResource : Resource {

    companion object {
        /**
         * Logger
         */
        @JvmStatic
        private val logger = LoggerFactory.getLogger(this::class.java)
    }


    override fun exists(): Boolean {
        // 1.如果是文件的话，那么使用文件的exists方法去进行判断
        if (isFile()) {
            try {
                return getFile().exists()
            } catch (ex: IOException) {
                if (logger.isDebugEnabled) {
                    logger.debug("无法解析到资源(File) [${getDescription()}]", ex)
                }
            }
        }

        // 2. fallback, 使用InputStream去进行判断
        try {
            getInputStream().close()
            return true
        } catch (ex: Throwable) {
            if (logger.isDebugEnabled) {
                logger.debug("无法解析到资源(InputStream) [${getDescription()}]", ex)
            }
        }
        return false
    }

    override fun getURI(): URI {
        try {
            return ResourceUtils.toURI(getURL())
        } catch (ex: URISyntaxException) {
            throw FileNotFoundException("给定的URL[${getURL()}]无法被解析成为URI")
        }
    }

    override fun getURL(): URL {
        throw FileNotFoundException("[${getDescription()}]无法被解析成为一个URL")
    }

    override fun getFile(): File = throw FileNotFoundException("给定的资源[${getDescription()}无法被解析成为一个文件]")

    // TODO
    override fun lastModified(): Long = -1

    // TODO
    override fun contentLength(): Long = -1

    override fun createRelative(relativePath: String): Resource {
        throw FileNotFoundException("无法根据给定的relativePath[$relativePath]去解析到基于[${getDescription()}]的资源")
    }

    override fun getFilename(): String? = null

    override fun toString() = getDescription()
}