package com.wanna.boot.loader

import com.wanna.boot.loader.archive.Archive
import java.io.File
import java.io.InputStream
import java.net.URL
import java.util.*

/**
 * 基于Properties配置文件的方式去对应用去进行启动的[Launcher]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/12
 */
open class PropertiesLauncher : Launcher() {

    companion object {

        /**
         * 只是存在有parentClassLoader的参数的参数类型列表
         */
        @JvmStatic
        private val PARENT_ONLY_PARAMS = arrayOf(ClassLoader::class.java)

        /**
         * 有URLs和parentClassLoader的参数的参数类型列表
         */
        @JvmStatic
        private val URLS_AND_PARENT_PARAMS = arrayOf(Array<URL>::class.java, ClassLoader::class.java)

        /**
         * 空URL数组
         */
        @JvmStatic
        private val NO_URLS = emptyArray<URL>()

        /**
         * 为参数的参数类型列表
         */
        @JvmStatic
        private val NO_PARAMS = emptyArray<Class<*>>()

        /**
         * 当前是否是Debug程序
         */
        private const val DEBUG = "loader.debug"

        /**
         * 主启动类的属性Key, 也可以通过Manifest的"Start-Class"去进行指定
         */
        const val MAIN = "loader.main"

        /**
         * Loader的配置文件路径
         */
        private const val CONFIG_LOCATION = "loader.config.location"

        /**
         * Loader的配置文件属性名
         */
        private const val CONFIG_NAME = "loader.config.name"
    }

    private val home: File

    /**
     * 存放相关配置信息的Properties
     */
    private val properties = Properties()

    private val parent: Archive


    init {
        try {
            this.home = this.getHomeDirectory()
            initializeProperties()
            initializePaths()
            this.parent = super.createArchive()
        } catch (ex: Exception) {
            throw IllegalStateException(ex)
        }
    }

    override fun getMainClass(): String {
        TODO("Not yet implemented")
    }

    override fun getClassPathArchivesIterator(): Iterator<Archive> {
        TODO("Not yet implemented")
    }

    override fun getArchive(): Archive {
        TODO("Not yet implemented")
    }

    protected open fun getHomeDirectory(): File {
        return File("${'$'}{user.dir}")
    }

    private fun initializePaths() {

    }

    private fun initializeProperties() {

        val configs = ArrayList<String>()

        for (config in configs) {
            val resource = getResource(config)
            if (resource == null) {
                debug("Not found: $config")
            } else {
                debug("Found: $config")
                loadResource(resource)
                // 只要加载到一个, 默认就行了, return
                return
            }
        }
    }

    /**
     * 如果开启了debug, 那么需要输出相关的message信息
     *
     * @param message message
     */
    private fun debug(message: String) {
        if (System.getProperty(DEBUG) == "true") {
            println(message)
        }
    }

    private fun getResource(config: String): InputStream? {
        return javaClass.getResourceAsStream(config)
    }


    private fun loadResource(resource: InputStream) {
        this.properties.load(resource)
    }


}