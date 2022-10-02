package com.wanna.boot.loader

import com.wanna.boot.loader.archive.Archive
import com.wanna.boot.loader.archive.ExplodedArchive
import com.wanna.boot.loader.archive.JarFileArchive
import java.io.File
import java.net.URL

/**
 * 用于启动整个应用的Launcher启动器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/26
 */
abstract class Launcher {

    companion object {
        /**
         * JarMode的启动类
         */
        const val JAR_MODE_LAUNCHER = "com.wanna.boot.loader.jarmode.JarModeLauncher"
    }

    /**
     * 供子类当中去创建main方法，并去完成进行调用
     *
     * @param args 启动应用时需要用到的方法参数列表(命令行参数)
     */
    protected open fun launch(args: Array<String>) {
        // 创建ClassLoader
        val classLoader = createClassLoader(getClassPathArchivesIterator())

        // 如果是jarmode，那么使用JarModeLauncher作为启动类
        // 如果不是jarmode，那么需要从Manifest当中去获取"Start-Class"
        val jarMode = System.getProperty("jarmode")
        val launchClass = if (jarMode != null && jarMode.isNotEmpty()) JAR_MODE_LAUNCHER else getMainClass()
        launch(args, launchClass, classLoader)
    }

    /**
     * 使用launchClass去启动整个应用
     *
     * @param args args
     * @param launchClass 启动类
     */
    protected open fun launch(args: Array<String>, launchClass: String, classLoader: ClassLoader) {
        Thread.currentThread().contextClassLoader = classLoader
        createMainMethodRunner(launchClass, args, classLoader).run()
    }

    /**
     * 创建MainMethodRunner对象
     *
     * @param mainClassName 主类名
     * @param args 命令行参数列表
     * @param classLoader ClassLoader
     * @return 创建好的MainMethodRunner
     */
    protected open fun createMainMethodRunner(
        mainClassName: String,
        args: Array<String>,
        classLoader: ClassLoader
    ): MainMethodRunner {
        return MainMethodRunner(mainClassName, args)
    }

    /**
     * 获取主类
     *
     * @return 主类
     */
    abstract fun getMainClass(): String

    /**
     * 获取ClassPath下的Archive的迭代器
     *
     * @return Archive归档文件的迭代器
     */
    @Throws(Exception::class)
    protected abstract fun getClassPathArchivesIterator(): Iterator<Archive>

    /**
     * 创建ClassLoader
     *
     * @param archives 归档文件对象列表
     * @return ClassLoader
     */
    protected open fun createClassLoader(archives: Iterator<Archive>): ClassLoader {
        val urls = ArrayList<URL>()
        archives.forEach { urls.add(it.getUrl()) }
        return createClassLoader(urls.toTypedArray())
    }

    /**
     * 根据URL去创建ClassLoader
     *
     * @param urls URLs
     * @return 创建好的ClassLoader
     */
    protected open fun createClassLoader(urls: Array<URL>): ClassLoader {
        return LaunchedURLClassLoader(isExploded(), getArchive(), urls, this::class.java.classLoader)
    }

    /**
     * 创建Archive归档对象
     *
     * @return 创建好的Archive对象
     */
    protected open fun createArchive(): Archive {
        // 获取当前类的ProtectionDomain
        val protectionDomain = javaClass.protectionDomain

        // 获取代码的位置，比如"/xxx/xxx/main/"
        val codeSource = protectionDomain.codeSource

        // 将代码的位置去转换成为URI
        val location = codeSource?.location?.toURI()
        val path = location?.schemeSpecificPart
            ?: throw IllegalStateException("Unable to determine code source archive")

        // 获取到URI当中的path转换成为文件
        val root = File(path)
        check(root.exists()) { "无法根据归档文件的位置[$root]去找到文件资源" }

        // 如果是个文件夹的话，创建ExplodedArchive，如果不是文件夹的话创建JarFileArchive
        return if (root.isDirectory) ExplodedArchive(root) else JarFileArchive(root)
    }

    /**
     * 该归档文件是否是被解压之后的？
     *
     * @return 如果是被解压之后的，那么return true
     */
    protected open fun isExploded(): Boolean = false

    /**
     * 获取Archive
     *
     * @return Archive
     */
    protected abstract fun getArchive(): Archive

}