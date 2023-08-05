package com.wanna.boot.gradle.tasks.bundling

import org.gradle.api.Action
import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.internal.file.copy.CopyAction
import org.gradle.api.provider.Property
import org.gradle.api.specs.Spec
import org.gradle.jvm.tasks.Jar
import java.io.File
import java.util.concurrent.Callable
import java.util.function.Function

/**
 * BootJar任务
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/5
 */
open class BootJar : Jar(), BootArchive {

    companion object {
        /**
         * JarLauncher, 引导整个SpringBoot的Jar包的启动
         */
        private const val LAUNCHER = "com.wanna.boot.loader.JarLauncher"

        private const val CLASSES_DIRECTORY = "BOOT-INF/classes"

        private const val LIB_DIRECTORY = "BOOT-INF/lib"

        private const val CLASSPATH_INDEX = "BOOT-INF/classpath.idx"
    }

    /**
     * 为构建SpringBoot的大Jar包提供支持
     */
    private val support = BootArchiveSupport(LAUNCHER, ZipCompressionResolver())

    /**
     * ClassPath
     */
    private var classpath: FileCollection? = null

    /**
     * 主类
     */
    private val mainClass = project.objects.property(String::class.java)

    /**
     * BootInfo Spec
     */
    private val bootInfoSpec = project.copySpec().into("BOOT-INF")

    init {
        // 对BOOT-INF去进行配置
        configureBootInfSpec(bootInfoSpec)

        // 核心代码...
        mainSpec.with(bootInfoSpec)
    }

    private fun <T> fromCallTo(callable: Callable<T>): Action<CopySpec> {
        return Action { it.from(callable) }
    }

    /**
     * 对"BOOT-INF"去进行配置
     *
     * @param bootInfoSpec BootInfo Spec
     */
    private fun configureBootInfSpec(bootInfoSpec: CopySpec) {
        // 把所有的ClassPath的文件夹, copy到classes目录下(这里存放的是获取所有文件夹的callback回调函数, 在这里执行时classpath还没完成初始化)
        bootInfoSpec.into("classes", fromCallTo(this::classpathDirectories))

        // 把所有的ClassPath下的Zip(Jar)文件, copy到lib目录下(这里存放的是获取所有Zip的callback回调函数, 在这里执行时classpath还没完成初始化)
        bootInfoSpec.into("lib", fromCallTo(this::classpathFiles)).eachFile(this.support::excludeNonZipFiles)

        // 将module-info.class移动到Root
        this.support.moveModuleInfoToRoot(bootInfoSpec)

        // 把"META-INF"相关信息copy到Root下
        moveMetaInfToRoot(bootInfoSpec)
    }


    /**
     * 列举出来ClassPath当中所有的目录(也就是项目字节码的存放位置, 比如"/path/to/build/classes/java/main")
     *
     * @return ClassPath当中的所有的目录
     */
    private fun classpathDirectories(): Iterable<File> {
        return classpathEntries(File::isDirectory)
    }

    /**
     * 列举出来ClassPath当中的所有的文件(也就是所有的Jar包)
     *
     * @return ClassPath当中的所有的文件
     */
    private fun classpathFiles(): Iterable<File> {
        return classpathEntries(File::isFile)
    }

    private fun classpathEntries(filter: Spec<File>): Iterable<File> {
        return if (this.classpath != null) this.classpath!!.filter(filter) else emptyList()
    }

    /**
     * 将"META-INF"相关信息移动到root目录下的"META-INF"下
     */
    private fun moveMetaInfToRoot(copySpec: CopySpec) {
        copySpec.eachFile {
            val path = it.relativeSourcePath.pathString
            if (path.startsWith("META-INF/") && path != "META-INF/aop.xml" && !path.endsWith(".kotlin_module")
                && !path.startsWith("META-INF/services/")
            ) {
                support.moveToRoot(it)
            }
        }
    }


    /**
     * 在copy方法执行时, 对Jar包的Manifest去进行自定义(父类当中标注了`@TaskAction`注解)
     *
     * Note: 这个方法会被Gradle自动去进行回调
     */
    override fun copy() {
        this.support.configureManifest(
            manifest,
            mainClass.get(),
            CLASSES_DIRECTORY,
            LIB_DIRECTORY,
            CLASSPATH_INDEX,
            null
        )
        super.copy()
    }

    /**
     * 创建[CopyAction], 去拷贝文件并生成Jar包(这里也会将SpringBootLoader去进行打包到最终大Jar当中去)
     *
     * @return CopyAction
     */
    override fun createCopyAction(): CopyAction {
        return this.support.createCopyAction(this, ResolvedDependencies())
    }

    override fun getMainClass(): Property<String> {
        return mainClass
    }

    override fun getClasspath(): FileCollection {
        return classpath ?: throw IllegalStateException("classpath is not available")
    }

    override fun setClasspath(classpath: Any) {
        this.classpath = project.files(classpath)
    }

    override fun setClasspath(classpath: FileCollection) {
        this.classpath = classpath
    }

    /**
     * 解析给定的文件需要使用的压缩状态(如果是Library的话, 那么需要使用未压缩的状态; 不是Library的话, 需要使用压缩状态)
     *
     * @param details 要去进行解析压缩状态的文件
     */
    protected open fun resolveZipCompression(details: FileCopyDetails): ZipCompression {
        return if (isLibrary(details)) ZipCompression.STORED else ZipCompression.DEFLATED
    }

    /**
     * 检查给定的文件是否是一个Library?
     *
     * @param details 要去进行检查的文件
     * @return 如果它的entryName是以"BOOT-INF/lib"作为开头的话, 说明它是Library; 否则就不是
     */
    protected open fun isLibrary(details: FileCopyDetails): Boolean {
        val path = details.relativePath.pathString
        return path.startsWith(LIB_DIRECTORY)
    }

    /**
     * ZipEntry的压缩状态的解析, 该文件可能需要使用压缩的状态, 也可能需要使用未压缩的状态, 因此这里需要去进行解析
     */
    private inner class ZipCompressionResolver : Function<FileCopyDetails, ZipCompression> {
        override fun apply(details: FileCopyDetails): ZipCompression {
            return resolveZipCompression(details)
        }
    }
}