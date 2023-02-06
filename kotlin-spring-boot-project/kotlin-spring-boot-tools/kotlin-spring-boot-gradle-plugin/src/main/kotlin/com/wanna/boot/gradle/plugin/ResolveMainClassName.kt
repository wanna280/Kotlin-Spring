package com.wanna.boot.gradle.plugin

import com.wanna.boot.loader.tools.MainClassFinder
import org.gradle.api.DefaultTask
import org.gradle.api.Transformer
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import javax.annotation.Nullable

/**
 * 用于去进行解析SpringBoot的主启动类的Gradle任务, 并将SpringBoot的主启动类去写入到文件当中去
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/5
 */
open class ResolveMainClassName : DefaultTask() {

    companion object {
        /**
         * `@SpringBootApplication`注解的类名, 我们通过这个注解, 去进行mainClass的解析
         */
        private const val SPRING_BOOT_APPLICATION_CLASS_NAME = "com.wanna.boot.autoconfigure.SpringBootApplication"
    }

    /**
     * 对于解析得到的主类的类名, 需要去进行输出的文件"resolveMainClassName"
     */
    private val outputFile: RegularFileProperty = project.objects.fileProperty()

    /**
     * 用户已经去进行手动配置的主类
     */
    private val configuredMainClass: Property<String> = project.objects.property(String::class.java)

    /**
     * ClassPath, 也就是自定义要去进行搜索主类的目录
     */
    private var classpath: FileCollection? = null

    open fun setClassPath(obj: Any) {
        this.classpath = project.files(obj)
    }

    open fun setClassPath(classpath: FileCollection) = this.setClassPath(classpath as Any)

    @Classpath
    open fun getClassPath(): FileCollection {
        return this.classpath ?: throw IllegalStateException("ClassPath FileCollection is not available")
    }

    /**
     * 当前Gradle任务需要去执行的操作,
     * 我们在这里去解析得到mainClass, 并写入到"resolveMainClassName"文件当中
     */
    @TaskAction
    open fun resolveAndStoreMainClassName() {
        val outputFile = this.outputFile.asFile.get()
        outputFile.parentFile.mkdirs()
        val mainClassName = resolveMainClassName()


        // 将解析得到主类, 去写入到"resolveMainClassName"文件当中去, 后续会从文件当中去进行读取主类
        Files.writeString(
            outputFile.toPath(), mainClassName,
            StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING
        )
    }

    @OutputFile
    open fun getOutputFile(): RegularFileProperty {
        return this.outputFile
    }

    @Input
    @Optional
    open fun getConfiguredMainClassName(): Property<String> {
        return this.configuredMainClass
    }

    /**
     * 执行解析SpringBoot应用的主启动类
     *
     * @return 解析得到的mainClass的类名
     */
    open fun resolveMainClassName(): String {

        // 检查是否有去进行自定义mainClass
        val mainClassName = configuredMainClass.orNull
        if (mainClassName != null) {
            return mainClassName
        }

        // 如果没有去进行自定义mainClass, 那么, 我们从类路径下去进行计算和推断得到合适的主类
        for (file in getClassPath().filter(File::isDirectory).files) {
            val mainClass = findMainClass(file)
            if (mainClass != null) {
                return mainClass
            }
        }
        return ""
    }

    /**
     * 读取主类名, 之前已经将解析到的主类写入文件, 因此这里从"resolveMainClassName"这个文件当中去进行读取
     *
     * @return 从文件当中去读取到的主类名
     */
    open fun readMainClassName(): Provider<String> {
        return outputFile.map(ClassNameReader())
    }

    /**
     * 从给定的文件夹下, 去找到标注了`@SpringBootApplication`注解的主类(如果没有的话, 普通的主类也可以)
     *
     * @return mainClass(找不到的话, return null)
     */
    @Nullable
    private fun findMainClass(file: File): String? {
        return MainClassFinder.findSingleMainClass(file, SPRING_BOOT_APPLICATION_CLASS_NAME)
    }


    /**
     * 将文件去转换成为字符串的[Transformer], 从文件当中读取到mainClass的类名
     */
    private class ClassNameReader : Transformer<String, RegularFile> {
        override fun transform(`in`: RegularFile): String {
            if (`in`.asFile.length() == 0.toLong()) {
                throw IllegalStateException("Main class name has not been configured and it could not be resolved")
            }
            val output = `in`.asFile.toPath()
            try {
                return Files.readString(output)
            } catch (ex: Exception) {
                throw IllegalStateException("Failed to read main class name from '$output'")
            }
        }
    }

}