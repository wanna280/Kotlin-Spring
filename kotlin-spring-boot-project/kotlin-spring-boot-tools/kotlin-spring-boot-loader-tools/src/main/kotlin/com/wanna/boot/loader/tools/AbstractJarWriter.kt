package com.wanna.boot.loader.tools

import org.apache.commons.compress.archivers.jar.JarArchiveEntry
import java.io.BufferedInputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.jar.JarEntry
import java.util.jar.JarInputStream
import java.util.jar.Manifest
import java.util.zip.ZipEntry
import javax.annotation.Nullable

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/5
 */
abstract class AbstractJarWriter : LoaderClassesWriter {
    companion object {

        private const val NESTED_LOADER_JAR = "META-INF/loader/kotlin-spring-boot-loader.jar"

        /**
         * BufferSize
         */
        private const val BUFFER_SIZE = 32 * 1024
    }

    /**
     * 已经写过的JarEntry列表
     */
    private val writtenEntries = LinkedHashSet<String>()

    /**
     * 写入Manifest Entry
     *
     * @param manifest Manifest
     */
    open fun writeManifest(manifest: Manifest) {
        writeEntry(JarArchiveEntry("META-INF/MANIFEST.MF"), manifest::write)
    }


    override fun writeLoaderClasses() = writeLoaderClasses(NESTED_LOADER_JAR)

    override fun writeLoaderClasses(loaderJarResourceName: String) {
        val loaderJar = javaClass.classLoader.getResource(loaderJarResourceName)
            ?: throw IllegalStateException("cannot find loader jar")

        // 将SpringBootLoader这个Jar包当中的全部Entry, 去写入到Jar当中
        JarInputStream(BufferedInputStream(loaderJar.openStream())).use {
            while (true) {
                val entry = it.nextJarEntry ?: break
                if (isDirectoryEntry(entry) || isClassEntry(entry)) {
                    writeEntry(JarArchiveEntry(entry), InputStreamEntryWriter(it))
                }
            }
        }
    }

    override fun writeEntry(name: String, inputStream: InputStream) {
        inputStream.use { writeEntry(name, InputStreamEntryWriter(it)) }
    }

    open fun writeEntry(entryName: String, entryWriter: EntryWriter) {
        writeEntry(JarArchiveEntry(entryName), entryWriter)
    }

    private fun isDirectoryEntry(entry: JarEntry): Boolean {
        return entry.isDirectory && entry.name != "META-INF/"
    }

    private fun isClassEntry(entry: JarEntry): Boolean {
        return entry.name.endsWith(".class")
    }

    private fun writeEntry(entry: JarArchiveEntry, entryWriter: EntryWriter) {
        writeEntry(entry, null, entryWriter, UnpackHandler.NEVER)
    }

    private fun writeEntry(
        entry: JarArchiveEntry,
        @Nullable library: Library?,
        @Nullable entryWriter: EntryWriter?,
        unpackHandler: UnpackHandler
    ) {
        if (this.writtenEntries.add(entry.name)) {
            // 写入所有的父文件夹的Entry
            writeParentDirectoryEntries(entry.name)

            // 将当前Entry写入到Archive当中
            writeToArchive(entry, entryWriter)
        }
    }

    /**
     * 将给定的ZipEntry, 写入到Archive归档文件(Jar包)当中
     *
     * @param zipEntry 需要去进行写入的Entry
     * @param entryWriter EntryWriter(为null代表不需要去进行写入)
     */
    protected abstract fun writeToArchive(zipEntry: ZipEntry, @Nullable entryWriter: EntryWriter?)

    /**
     * 写入给定的EntryName对应的所有parent文件夹到归档文件当中
     *
     * @param name entryName
     */
    protected open fun writeParentDirectoryEntries(name: String) {
        var parent = if (name.endsWith("/")) name.substring(0, name.length - 1) else name
        while (true) {
            val lastIndex = parent.lastIndexOf("/")
            if (lastIndex == -1) {
                return
            }
            parent = parent.substring(0, lastIndex)
            if (parent.isNotEmpty()) {
                // entryWriter=null, no need to write
                writeEntry(JarArchiveEntry("$parent/"), null, null, UnpackHandler.NEVER)
            }
        }
    }

    interface UnpackHandler {

        companion object {
            @JvmStatic
            val NEVER = object : UnpackHandler {

            }
        }

    }

    /**
     * 基于InputStream的EntryWriter, 将给定的InputStream当中的内容, 全部写出到JarEntry当中
     */
    private data class InputStreamEntryWriter(private val inputStream: InputStream) : EntryWriter {

        override fun write(outputStream: OutputStream) {
            val buffer = ByteArray(BUFFER_SIZE)
            while (true) {
                val size = inputStream.read(buffer)
                if (size == -1) {
                    break
                }
                outputStream.write(buffer, 0, size)
            }
            outputStream.flush()
        }
    }
}