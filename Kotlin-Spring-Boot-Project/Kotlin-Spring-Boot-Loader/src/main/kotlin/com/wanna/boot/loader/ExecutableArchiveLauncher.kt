package com.wanna.boot.loader

import com.wanna.boot.loader.archive.Archive
import java.net.URL

/**
 * 所有的可执行的Java归档文件的启动器的基础类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/9/26
 * @see JarLauncher
 * @see WarLauncher
 */
abstract class ExecutableArchiveLauncher() : Launcher() {
    companion object {
        const val START_CLASS_ATTRIBUTE = "Start-Class"
        const val BOOT_CLASSPATH_INDEX_ATTRIBUTE = "Spring-Boot-Classpath-Index"
        const val DEFAULT_CLASSPATH_INDEX_FILE_NAME = "classpath.idx"
    }

    private var archive: Archive


    init {
        this.archive = this.createArchive()
    }


    /**
     * ClassPathIndexFile
     */
    private val classPathIndex =
        ClassPathIndexFile.loadIfPossible(archive.getUrl(), getClassPathIndexFileLocation(archive))


    /**
     * 提供一个自定义Archive的构造器，使用无参数构造器的话，可以去自动推断Archive
     *
     * @param archive Archive归档对象
     */
    constructor(archive: Archive) : this() {
        this.archive = archive
    }

    /**
     * 获取ClassPath的Archive的迭代器
     *
     * @return Archive迭代器
     */
    override fun getClassPathArchivesIterator(): Iterator<Archive> {
        return java.util.ArrayList<Archive>().iterator()
    }

    /**
     * 重写父类的创建ClassLoader的逻辑
     *
     * @param archives Archive归档对象的迭代器
     * @return ClassLoader
     */
    override fun createClassLoader(archives: Iterator<Archive>): ClassLoader {
        val urls = ArrayList<URL>()
        if (classPathIndex != null) {
            urls.addAll(classPathIndex.getUrls())
        }
        return createClassLoader(urls.toTypedArray())
    }

    override fun getArchive(): Archive = this.archive

    /**
     * 获取ClassPathIndexFile的位置，如果Manifest当中自定义了"Spring-Boot-Classpath-Index"，那么就采用给定的路径
     * 去作为ClassPathIndexFile的位置，如果没有自定义，那么就获取归档的Entry路径前缀拼上"classpath.idx"去作为默认的路径，
     * 例如Jar的话就是"BOOT-INF/classpath.idx"，War的话就是"WEB-INF/classpath.idx"
     *
     * @param archive 候选的归档文件
     * @return ClassPathIndexFile的位置
     */
    private fun getClassPathIndexFileLocation(archive: Archive): String {
        val manifest = archive.getManifest()
        return manifest.mainAttributes?.getValue(BOOT_CLASSPATH_INDEX_ATTRIBUTE)
            ?: (getArchiveEntryPathPrefix() + DEFAULT_CLASSPATH_INDEX_FILE_NAME)
    }

    /**
     * 获取归档文件的路径前缀，模板方法，交给子类去进行实现
     *
     * @return 归档文件的路径前缀
     */
    abstract fun getArchiveEntryPathPrefix(): String

    /**
     * 获取MainClass，从Manifest当中去进行获取到"Start-Class"属性，
     * 也就是获取到SpringBoot的主启动类
     *
     * @return mainClass
     * @throws IllegalStateException 如果获取不到Start-Class属性
     */
    @kotlin.jvm.Throws(IllegalStateException::class)
    override fun getMainClass(): String {
        val manifest = archive.getManifest()
        return manifest.mainAttributes.getValue(START_CLASS_ATTRIBUTE)
            ?: throw IllegalStateException("无法从Manifest当中去获取到Start-Class属性")
    }

}