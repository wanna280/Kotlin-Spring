package com.wanna.boot.loader.tools

import org.apache.commons.compress.archivers.jar.JarArchiveEntry
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.file.attribute.FileTime
import java.util.zip.ZipEntry
import javax.annotation.Nullable

/**
 * JarWriter
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/5
 *
 * @param file 要去进行输出的Jar文件
 * @param lastModifiedTime 最后一次修改时间
 */
open class JarWriter @JvmOverloads constructor(
    file: File,
    @Nullable launchScript: LaunchScript? = null,
    @Nullable private val lastModifiedTime: FileTime? = null
) : AbstractJarWriter(), AutoCloseable {

    private val jarOutputStream = JarArchiveOutputStream(FileOutputStream(file))

    init {
        if (launchScript != null) {
            this.jarOutputStream.writePreamble(launchScript.toByteArray())
            file.setExecutable(true)
        }
        jarOutputStream.encoding = "UTF-8"
    }

    /**
     * 将给定的ZipEntry, 写入到Archive归档文件(Jar包)当中
     *
     * @param zipEntry 需要去进行写入的Entry
     * @param entryWriter EntryWriter(为null代表不需要去进行写入)
     */
    override fun writeToArchive(zipEntry: ZipEntry, @Nullable entryWriter: EntryWriter?) {
        val jarArchiveEntry = asJarArchiveEntry(zipEntry)
        if (lastModifiedTime != null) {
            jarArchiveEntry.lastModifiedTime = lastModifiedTime
        }

        jarOutputStream.putArchiveEntry(jarArchiveEntry)
        entryWriter?.write(jarOutputStream)

        jarOutputStream.closeArchiveEntry()
    }

    private fun asJarArchiveEntry(zipEntry: ZipEntry): JarArchiveEntry {
        if (zipEntry is JarArchiveEntry) {
            return zipEntry
        }
        return JarArchiveEntry(zipEntry)
    }

    override fun close() {
        this.jarOutputStream.close()
    }
}