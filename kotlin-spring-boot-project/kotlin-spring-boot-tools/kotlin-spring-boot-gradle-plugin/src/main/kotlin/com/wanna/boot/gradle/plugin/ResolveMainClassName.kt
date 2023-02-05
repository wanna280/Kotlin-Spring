package com.wanna.boot.gradle.plugin

import com.wanna.boot.loader.tools.MainClassFinder
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*
import java.io.File
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
         * SpringBootApplication注解的类名
         */
        private const val SPRING_BOOT_APPLICATION_CLASS_NAME = "com.wanna.boot.autoconfigure.SpringBootApplication"
    }


    private val outputFile: RegularFileProperty = project.objects.fileProperty()

    private val configuredMainClass: Property<String> = project.objects.property(String::class.java)

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
     * 当前Gradle任务需要去执行的操作
     */
    @TaskAction
    open fun resolveAndStoreMainClassName() {
        val outputFile = this.outputFile.asFile.get()
        outputFile.parentFile.mkdirs()
        val mainClassName = resolveMainClassName()
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
     */
    open fun resolveMainClassName(): String {
        val mainClassName = configuredMainClass.orNull
        if (mainClassName != null) {
            return mainClassName
        }
        for (file in getClassPath().filter(File::isDirectory).files) {
            val mainClass = findMainClass(file)
            if (mainClass != null) {
                return mainClass
            }
        }
        return ""
    }

    open fun readMainClassName(): Provider<String> {
        return configuredMainClass
    }

    /**
     * 从给定的文件夹下, 去找到标注了`@SpringBootApplication`注解的主类
     *
     * @return mainClass(找不到的话, return null)
     */
    @Nullable
    private fun findMainClass(file: File): String? {
        return MainClassFinder.findSingleMainClass(file, SPRING_BOOT_APPLICATION_CLASS_NAME)
    }
}