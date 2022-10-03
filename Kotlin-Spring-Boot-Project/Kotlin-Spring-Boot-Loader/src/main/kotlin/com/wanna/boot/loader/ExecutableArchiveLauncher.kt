package com.wanna.boot.loader

import com.wanna.boot.loader.archive.Archive
import com.wanna.boot.loader.archive.ExplodedArchive
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

    /**
     * 当前Launcher所在的Archive归档对象
     *
     * @see com.wanna.boot.loader.archive.JarFileArchive
     * @see com.wanna.boot.loader.archive.ExplodedArchive
     */
    private var archive = this.createArchive()

    /**
     * ClassPathIndexFile，维护了ClassPath的文件列表
     */
    private val classPathIndex = this.getClassPathIndex(archive)

    /**
     * 提供一个自定义Archive的构造器，使用无参数构造器的话，可以去自动推断Archive
     *
     * @param archive Archive归档对象
     */
    constructor(archive: Archive) : this() {
        this.archive = archive
    }

    /**
     * 获取ClassPathIndex，只有ExplodedArchive才需要去获取ClassPathIndex
     *
     * @param archive 归档文件
     * @return ClassPathIndexFile
     */
    protected open fun getClassPathIndex(archive: Archive): ClassPathIndexFile? {
        // 只有ExplodedArchive，才需要去加载ClassPathIndexFile
        return if (archive is ExplodedArchive) {
            ClassPathIndexFile.loadIfPossible(archive.getUrl(), getClassPathIndexFileLocation(archive))
        } else {
            null
        }
    }

    /**
     * 获取ClassPath下的的Archive的迭代器，从归档文件内部去寻找嵌套的归档文件
     *
     * @return 内部搜索到的归档文件Archive的迭代器
     */
    override fun getClassPathArchivesIterator(): Iterator<Archive> {
        val searchFilter = object : Archive.EntryFilter {
            override fun matches(entry: Archive.Entry) = isSearchCandidate(entry)
        }

        // 这里需要搜索的是没有在ClassPathIndex当中的归档
        // 对于JarLauncher来说，!isEntryIndexed()一定为true，因此相当于这个条件直接忽略掉...
        // 对于WarLauncher来说，就需要排除掉ClassPathIndex当中的...
        val includeFilter = object : Archive.EntryFilter {
            override fun matches(entry: Archive.Entry) =
                isNestedArchive(entry) && !isEntryIndexed(entry)
        }

        // 搜索得到当前归档文件内部嵌套的归档文件列表
        var archives = archive.getNestedArchives(searchFilter, includeFilter)

        // 如果需要去进行后置处理的话，那么去进行自定义逻辑的处理
        if (isPostProcessingClassPathArchives()) {
            archives = applyClassPathArchivePostProcessing(archives)
        }
        return archives
    }

    /**
     * 检查一个归档文件的Entry是否是一个需要去进行搜索的Entry
     *
     * @param entry 待匹配的Entry
     * @return 如果该归档文件匹配的话，return true；否则return false
     */
    protected open fun isSearchCandidate(entry: Archive.Entry): Boolean =
        entry.getName().startsWith(getArchiveEntryPathPrefix())

    /**
     * 检查给定的ArchiveEntry是否是一个嵌套的归档文件？
     *
     * @param entry 待匹配的ArchiveEntry
     * @return 如果它是一个嵌套的归档文件，那么return true；否则return false
     */
    protected open fun isNestedArchive(entry: Archive.Entry): Boolean {
        return false
    }

    /**
     * 是否需要对ClassPath下搜索的嵌套归档文件去进行后置处理
     *
     * @return 如果需要的话，那么return true；不需要的话，return false
     */
    protected open fun isPostProcessingClassPathArchives(): Boolean {
        return true
    }

    /**
     * 对ClassPath下搜索的嵌套归档文件去进行后置处理
     *
     * @param archives 搜索得到的归档文件列表
     * @return 处理之后的归档文件列表
     */
    protected open fun applyClassPathArchivePostProcessing(archives: Iterator<Archive>): Iterator<Archive> {
        val archiveList = ArrayList<Archive>()
        archives.forEach(archiveList::add)
        postProcessClassPathArchives(archiveList)
        return archiveList.iterator()
    }

    /**
     * 对ClassPath下搜索的嵌套归档文件去进行后置处理
     *
     * @param archives 搜索得到的归档文件的列表
     */
    protected open fun postProcessClassPathArchives(archives: MutableList<Archive>) {

    }

    /**
     * 检查给定的ArchiveEntry是否在ClassPathIndex当中？
     *
     * @param entry 待匹配的ArchiveEntry
     * @return 如果它在ClassPathIndex当中，那么return true；否则return false
     */
    protected open fun isEntryIndexed(entry: Archive.Entry): Boolean =
        classPathIndex?.containsEntry(entry.getName()) ?: false

    /**
     * 重写父类的创建ClassLoader的逻辑
     *
     * @param archives Archive归档对象的迭代器
     * @return ClassLoader
     */
    override fun createClassLoader(archives: Iterator<Archive>): ClassLoader {
        val urls = ArrayList<URL>()

        // 1.添加Archive当中的URL
        archives.forEach { urls.add(it.getUrl()) }

        // 2.添加ClassPathIndex当中的URL
        if (classPathIndex != null) {
            urls.addAll(classPathIndex.getUrls())
        }

        // 3.根据URL去创建ClassLoader
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