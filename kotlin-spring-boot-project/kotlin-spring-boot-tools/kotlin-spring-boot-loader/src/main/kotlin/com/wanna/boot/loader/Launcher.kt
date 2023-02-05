package com.wanna.boot.loader

import com.wanna.boot.loader.archive.Archive
import com.wanna.boot.loader.archive.ExplodedArchive
import com.wanna.boot.loader.archive.JarFileArchive
import com.wanna.boot.loader.jar.JarFile
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
        const val JAR_MODE_LAUNCHER = "JarModeLauncher"
    }

    /**
     * 供子类当中去创建main方法, 并去完成进行调用
     *
     * @param args 启动应用时需要用到的方法参数列表(命令行参数)
     * @throws Exception 如果启动应用失败
     */
    @Throws(Exception::class)
    protected open fun launch(args: Array<String>) {
        // 如果当前不是一个解压的包的话, 那么需要注册URLProtocolHandler
        if (!isExploded()) {
            JarFile.registerUrlProtocolHandler()
        }

        // 创建ClassLoader
        val classLoader = createClassLoader(getClassPathArchivesIterator())

        // 如果是jarmode, 那么使用JarModeLauncher作为启动类
        // 如果不是jarmode, 那么需要从Manifest当中去获取"Start-Class"去作为主启动类
        val jarMode = System.getProperty("jarmode")
        val launchClass = if (jarMode != null && jarMode.isNotEmpty()) JAR_MODE_LAUNCHER else getMainClass()
        launch(args, launchClass, classLoader)
    }

    /**
     * 使用launchClass去启动整个SpringBoot应用
     *
     * @param args args
     * @param launchClass 启动类
     */
    @Throws(Exception::class)
    protected open fun launch(args: Array<String>, launchClass: String, classLoader: ClassLoader) {
        // 设置Thread ContextClassLoader, 后续加载main方法所在的类时, 就会使用到这个ClassLoader
        Thread.currentThread().contextClassLoader = classLoader

        // 创建Main方法的Runner, 并执行run方法启动应用
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
     * 获取ClassPath下的Archive归档文件的迭代器
     *
     * @return Archive归档文件的迭代器
     */
    @Throws(Exception::class)
    protected abstract fun getClassPathArchivesIterator(): Iterator<Archive>

    /**
     * 根据搜索得到的嵌套的归档文件列表, 去创建ClassLoader
     *
     * @param archives 搜索得到的嵌套归档文件对象列表
     * @return 创建好的ClassLoader(LaunchedURLClassLoader)
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
     * 创建Java的Archive归档对象
     *
     * @return 创建好的Archive对象
     */
    protected open fun createArchive(): Archive {
        // 获取当前类的ProtectionDomain
        val protectionDomain = javaClass.protectionDomain

        // 获取代码的位置, 比如"/xxx/xxx/main/"(或者是/path/to/xxx.jar)
        val codeSource = protectionDomain.codeSource

        // 将代码的位置去转换成为URI
        val location = codeSource?.location?.toURI()
        val path = location?.schemeSpecificPart
            ?: throw IllegalStateException("无法找到CodeSource的归档文件")

        // 获取到URI当中的path转换成为文件
        val root = File(path)
        check(root.exists()) { "无法根据归档文件的位置[$root]去找到文件资源" }

        // 如果是个文件夹的话, 创建ExplodedArchive, 如果不是文件夹的话创建JarFileArchive
        return if (root.isDirectory) ExplodedArchive(root) else JarFileArchive(root)
    }

    /**
     * 该归档文件是否是被解压之后的?
     *
     * @return 如果是被解压之后的, 那么return true
     */
    protected open fun isExploded(): Boolean = false

    /**
     * 获取当前启动器的Archive归档文件对象
     *
     * @return Archive归档文件对象
     */
    protected abstract fun getArchive(): Archive

}