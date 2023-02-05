package com.wanna.boot.gradle.tasks.bundling

import org.apache.commons.compress.archivers.zip.UnixStat
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.annotation.Nullable

/**
 * 内部用于去将SpringBootLoader的JarEntry去进行拷贝的工具类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/5
 */
class LoaderZipEntries @JvmOverloads constructor(@Nullable private val entryTime: Long? = null) {

    /**
     * 将SpringBootLoader的类, 去写入到Archive归档文件当中
     *
     * @param out 归档文件的输出流
     */
    fun writeTo(out: ZipArchiveOutputStream): WrittenEntries {
        val writtenEntries = WrittenEntries()
        val loaderJarFile = javaClass.getResourceAsStream("/META-INF/loader/kotlin-spring-boot-loader.jar")
            ?: throw IllegalStateException("cannot find spring-boot-loader jar")

        ZipInputStream(loaderJarFile).use {
            var entry = it.nextEntry
            while (entry != null) {
                if (entry.isDirectory && entry.name != "META-INF/") {
                    writeDirectory(ZipArchiveEntry(entry), out)
                    writtenEntries.addDirectory(entry)
                } else if (entry.name.endsWith(".class")) {
                    writeClass(ZipArchiveEntry(entry), it, out)
                    writtenEntries.addFile(entry)
                }
                entry = it.nextEntry
            }
        }
        return writtenEntries
    }

    private fun writeDirectory(entry: ZipArchiveEntry, out: ZipArchiveOutputStream) {
        prepareEntry(entry, UnixStat.DIR_FLAG or UnixStat.DEFAULT_DIR_PERM)
        out.putArchiveEntry(entry)
        out.closeArchiveEntry()
    }

    private fun writeClass(entry: ZipArchiveEntry, `in`: ZipInputStream, out: ZipArchiveOutputStream) {
        prepareEntry(entry, UnixStat.FILE_FLAG or UnixStat.DEFAULT_FILE_PERM)
        out.putArchiveEntry(entry)
        out.write(`in`.readAllBytes())
        out.closeArchiveEntry()
    }

    private fun prepareEntry(entry: ZipArchiveEntry, unixMode: Int) {
        if (entryTime != null) {
            entry.time = entryTime
        }

        entry.unixMode = unixMode
    }

    class WrittenEntries {

        fun addFile(entry: ZipEntry) {

        }

        fun addDirectory(entry: ZipEntry) {

        }
    }

}