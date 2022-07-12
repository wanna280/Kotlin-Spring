package com.wanna.boot.devtools.restart

import com.wanna.boot.devtools.settings.DevToolsSettings
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.management.ManagementFactory
import java.net.URL
import java.net.URLClassLoader

/**
 * 从给定的URL当中，去判断有哪些URL是要去交给RestartClassLoader去进行类加载的？
 *
 * @param urls 候选的需要去进行过滤的URL
 */
class ChangeableUrls(urls: Array<URL>) : Iterable<URL> {

    // "DevTools"去进行Restart时，应该使用的URL的列表
    private val urls = ArrayList<URL>()

    init {
        // 获取DevTools的配置信息
        val settings = DevToolsSettings.get()

        // 对给出的URL列表去进行过滤，过滤出来需要去apply给Restart的URL的列表
        this.urls += urls.filter {
            (settings.isRestartInclude(it) || isDirectoryUrl(it.toString())) && !settings.isRestartExclude(it)
        }.toList()

        if (logger.isDebugEnabled) {
            logger.debug("匹配到需要去进行重新加载的URL列表为:[${this.urls}]")
        }
    }

    override fun iterator(): Iterator<URL> = urls.iterator()

    companion object {
        // Logger
        private val logger = LoggerFactory.getLogger(ChangeableUrls::class.java)

        /**
         * 从一个ClassLoader当中去获取到应该apply到Restart的URL
         *
         * @param classLoader ClassLoader
         * @return 要去进行使用的URL
         */
        @JvmStatic
        fun fromClassLoader(classLoader: ClassLoader): ChangeableUrls {
            val urls = ArrayList<URL>()
            // 如果它是一个URLClassLoader的话，那么直接获取它的URL
            if (classLoader is URLClassLoader) {
                urls += classLoader.urLs
                // 如果它不是一个URL，那么获取Runtime的ClassPath去进行推测作为URL
            } else {
                urls += ManagementFactory.getRuntimeMXBean().classPath
                    .split(File.pathSeparator)
                    .map { File(it).toURI().toURL() }
            }

            // 构建ChangeableUrls，去过滤出来合适的URL，apply给Restarter
            return ChangeableUrls(urls.toTypedArray())
        }

        /**
         * 给定一个urlString，判断该文件夹是否是一个文件夹
         *
         * @param urlString urlString
         * @return 如果它是一个文件夹return true；否则return false
         */
        @JvmStatic
        private fun isDirectoryUrl(urlString: String): Boolean =
            urlString.startsWith("file:") && urlString.endsWith("/")
    }
}