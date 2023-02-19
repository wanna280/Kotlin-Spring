package com.wanna.debugger.bistoury.instrument.client.classpath

import java.io.File

/**
 * 应用程序当中需要去进行使用的ClassPath的计算的Supplier
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/4
 *
 * @param appLibClassSupplier appLibClassSuppler, 用于去计算libClass
 */
open class DefaultAppClassPathSupplier(private val appLibClassSupplier: AppLibClassSupplier) : AppClassPathSupplier {

    companion object {
        /**
         * AppClasses路径, 指定的是项目的代码的字节码存放的位置, 一般情况下为"/classes"目录下
         */
        private const val BISTOURY_APP_CLASSES_PATH = "bistoury.app.classes.path"
    }

    /**
     * 对于正常的应用来说, 目录结构如下：
     * * /ROOT
     *      * /lib
     *          * /xxx.jar
     *          * /yyy.jar
     *      * /classes
     *
     * 对于SpringBoot的Jar包的应用来说, 目录结构如下:
     * * /BOOT-INF
     *      * /lib
     *           * /xxx.jar
     *           * /yyy.jar
     *      * /classes
     */
    private val supplier: AppClassPathSupplier

    init {
        val appLibClass = appLibClassSupplier.get()

        // 获取AppLibClass所在的Jar包的代码位置
        val appLibUrl = appLibClass.protectionDomain.codeSource.location
        val libJarPath = appLibUrl.toString()

        // 计算得到Jar包所在的目录(lib目录)
        val appLibDirectory = File(libJarPath).parentFile.absolutePath

        // 解压SpringBoot的Jar包, 通过Manifest去计算得到SpringBoot应用的Jar包依赖的存放的位置("BOOT-INF/lib/")
        val springBootJarLibPath = JarStorePathUtils.getJarLibPath()
        // 解析SpringBoot的Jar包, 通过Manifest去计算得到SpringBoot应用程序类存放的位置("BOOT-INF/classes/")
        val springBootClassesPath = JarStorePathUtils.getJarClassesPath()

        // 如果通过"bistoury.app.classes.path"去明确指定了classes路径的话, 那么直接使用该路径即可
        val appClassesPath = System.getProperty(BISTOURY_APP_CLASSES_PATH)
        if (!appClassesPath.isNullOrBlank()) {
            supplier = SettableAppClassPathSupplier(
                listOf(
                    appLibDirectory, appClassesPath, springBootJarLibPath, springBootClassesPath
                )
            )

            // 如果没有通过"bistoury.app.classes.path"去指定classes路径的话, 那么我们需要直接使用appLib的上一级目录的"/classes"下去作为ClassPath目录...
        } else {
            supplier = WebAppClassPathSupplier(appLibDirectory, springBootJarLibPath, springBootClassesPath)
        }
    }

    override fun get(): List<String> = supplier.get()
}