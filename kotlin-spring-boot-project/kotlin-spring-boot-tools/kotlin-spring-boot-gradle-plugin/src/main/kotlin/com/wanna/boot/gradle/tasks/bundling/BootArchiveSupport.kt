package com.wanna.boot.gradle.tasks.bundling

import org.gradle.api.file.CopySpec
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.internal.file.copy.CopyAction
import org.gradle.api.java.archives.Manifest
import org.gradle.jvm.tasks.Jar
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.function.Function
import javax.annotation.Nullable

/**
 * 为[BootArchive]提供支持的类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/5
 */
class BootArchiveSupport(
    private val loaderMainClass: String,
    private val compressionResolver: Function<FileCopyDetails, ZipCompression>
) {
    companion object {
        /**
         * Zip文件的Header
         */
        @JvmStatic
        private val ZIP_FILE_HEADER = byteArrayOf('P'.toByte(), 'K'.toByte(), 3, 4)
    }


    /**
     * 对于Jar包的Manifest去进行配置
     *
     * @param manifest Gradle的Manifest
     * @param mainClass SpringBoot的主启动类
     * @param classes SpringBoot的classes目录
     * @param lib SpringBoot的lib目录
     * @param classpathIndex SpringBoot的ClassPath索引文件
     */
    fun configureManifest(
        manifest: Manifest,
        mainClass: String,
        classes: String,
        lib: String,
        @Nullable classpathIndex: String?,
        @Nullable layerIndex: String?
    ) {
        val attributes = manifest.attributes
        attributes.putIfAbsent("Main-Class", loaderMainClass)
        attributes.putIfAbsent("Start-Class", mainClass)
        attributes.putIfAbsent("Spring-Boot-Classes", classes)
        attributes.putIfAbsent("Spring-Boot-Lib", lib)
        if (classpathIndex != null) {
            attributes.putIfAbsent("Spring-Boot-ClassPath-Index", classpathIndex)
        }
        if (layerIndex != null) {
            attributes.putIfAbsent("Spring-Boot-Layers-Index", layerIndex)
        }
    }

    fun createCopyAction(jar: Jar, resolvedDependencies: ResolvedDependencies): CopyAction {
        val file = jar.archiveFile.get().asFile
        return BootZipCopyAction(file, this.compressionResolver)
    }

    /**
     * 将module-info.class移动到Archive的根目录下
     *
     * @param spec copySpec
     */
    fun moveModuleInfoToRoot(spec: CopySpec) {
        spec.filesMatching("module-info.class", this::moveToRoot)
    }

    /**
     * 将给定的文件, 去移动到Archive的根目录下
     *
     * @param details 要去进行移动的文件
     */
    fun moveToRoot(details: FileCopyDetails) {
        // 将文件所处的位置去设置为sourcePath
        details.relativePath = details.relativeSourcePath
    }

    /**
     * 排除掉所有的非Zip文件, 如果不是Zip文件, 需要去进行排除掉
     *
     * @param details 待检查的文件
     */
    fun excludeNonZipFiles(details: FileCopyDetails) {
        if (!isZip(details.file)) {
            details.exclude()
        }
    }

    private fun isZip(file: File): Boolean {
        try {
            FileInputStream(file).use {
                if (isZip(it)) {
                    return true
                }
            }
        } catch (ignored: Exception) {
            // ignore
        }
        return false
    }

    private fun isZip(inputStream: InputStream): Boolean {
        for (byte in ZIP_FILE_HEADER) {
            if (inputStream.read() != byte.toInt()) {
                return false
            }
        }
        return true
    }
}