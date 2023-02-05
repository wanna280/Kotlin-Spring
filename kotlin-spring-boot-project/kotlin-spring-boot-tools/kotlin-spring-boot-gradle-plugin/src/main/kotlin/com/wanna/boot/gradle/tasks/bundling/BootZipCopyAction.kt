package com.wanna.boot.gradle.tasks.bundling

import org.apache.commons.compress.archivers.zip.UnixStat
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
import org.gradle.api.GradleException
import org.gradle.api.file.FileCopyDetails
import org.gradle.api.internal.file.copy.CopyAction
import org.gradle.api.internal.file.copy.CopyActionProcessingStream
import org.gradle.api.tasks.WorkResult
import org.gradle.api.tasks.WorkResults
import java.io.*
import java.util.function.Function
import java.util.zip.CRC32
import java.util.zip.ZipEntry
import javax.annotation.Nullable

/**
 * BootZip的拷贝的Action, 将引导Jar包去copy到最终的产物当中
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/5
 */
class BootZipCopyAction(
    private val output: File,
    private val compressionResolver: Function<FileCopyDetails, ZipCompression>
) : CopyAction {


    override fun execute(copyActions: CopyActionProcessingStream): WorkResult {
        try {
            writeArchive(copyActions)
            return WorkResults.didWork(true)
        } catch (ex: IOException) {
            throw GradleException("fail to create $output", ex)
        }
    }

    private fun writeArchive(copyActions: CopyActionProcessingStream) {
        FileOutputStream(output).use { writeArchive(copyActions, it) }
    }

    private fun writeArchive(copyActions: CopyActionProcessingStream, output: OutputStream) {
        ZipArchiveOutputStream(output).use { zipOut ->
            val processor = Processor(zipOut)
            copyActions.process(processor::process)
            processor.finish()
        }
    }

    private class CrcAndSize(inputStream: InputStream) {

        companion object {
            private const val BUFFER_SIZE = 32 * 1024
        }

        private val crc = CRC32()

        private var size = 0L

        init {
            load(inputStream)
        }

        private fun load(inputStream: InputStream) {
            val buffer = ByteArray(BUFFER_SIZE)
            while (true) {
                val read = inputStream.read(buffer)
                if (read == -1) {
                    return
                }
                this.crc.update(buffer, 0, read)
                this.size += read
            }
        }

        /**
         * 将size和crc应用给ZipEntry当中
         *
         * @param entry 要去进行应用的Entry
         */
        fun setUpStoredEntry(entry: ZipArchiveEntry) {
            entry.size = size
            entry.compressedSize = size
            entry.crc = this.crc.value
            entry.method = ZipEntry.STORED
        }
    }


    inner class Processor(private val out: ZipArchiveOutputStream) {

        private var writtenEntries: LoaderZipEntries.WrittenEntries? = null

        private val writtenDirectories = LinkedHashSet<String>()

        fun process(details: FileCopyDetails) {
            // 如果必要的话, 先将loader的相关Entry去写入到ZIP包当中
            writeLoaderEntriesIfNecessary(details)

            if (details.isDirectory) {
                processDirectory(details)
            } else {
                processFile(details)
            }
        }

        fun writeLoaderEntriesIfNecessary(details: FileCopyDetails) {
            // 如果写入过Loader的类了, 那么pass...
            if (writtenEntries != null) {
                return
            }

            val loaderZipEntries = LoaderZipEntries()
            this.writtenEntries = loaderZipEntries.writeTo(this.out)
        }

        private fun processDirectory(details: FileCopyDetails) {
            val name = details.relativePath.pathString
            val entry = ZipArchiveEntry("$name/")
            prepareEntry(entry, name, details.lastModified, UnixStat.FILE_FLAG or details.mode)
            this.out.putArchiveEntry(entry)
            this.out.closeArchiveEntry()
            this.writtenEntries?.addDirectory(entry)
        }

        private fun processFile(details: FileCopyDetails) {
            val name = details.relativePath.pathString
            val entry = ZipArchiveEntry(name)
            prepareEntry(entry, name, details.lastModified, UnixStat.FILE_FLAG or details.mode)

            val zipCompression = compressionResolver.apply(details)

            // 如果需要使用未压缩的, 那么...先将存储方式设置为STORED, 并设置CRC和size
            if (zipCompression == ZipCompression.STORED) {
                prepareStoredEntry(details, entry)
            }

            this.out.putArchiveEntry(entry)
            details.copyTo(this.out)
            this.out.closeArchiveEntry()
        }

        private fun prepareStoredEntry(details: FileCopyDetails, entry: ZipArchiveEntry) {
            prepareStoredEntry(details.open(), entry)
        }

        /**
         * 为给定的ZipEntry, 去为不进行压缩去做准备, 初始化CRC和size
         *
         * @param inputStream InputStream
         * @param entry 要去进行准备的ZipEntry
         */
        private fun prepareStoredEntry(inputStream: InputStream, entry: ZipArchiveEntry) {
            CrcAndSize(inputStream).setUpStoredEntry(entry)
        }

        private fun prepareEntry(entry: ZipArchiveEntry, name: String, @Nullable time: Long?, mode: Int) {
            writeParentDirectoriesIfNecessary(name, time)
            entry.unixMode = mode
            if (time != null) {
                entry.time = time
            }
        }

        private fun writeParentDirectoriesIfNecessary(name: String, @Nullable time: Long?) {
            val parentDirectory: String? =
                if (name.lastIndexOf('/') == -1) null else name.substring(0, name.lastIndexOf('/'))

            if (parentDirectory != null && this.writtenDirectories.add(parentDirectory)) {
                val parentEntry = ZipArchiveEntry("$parentDirectory/")
                prepareEntry(parentEntry, name, time, UnixStat.DIR_FLAG or UnixStat.DEFAULT_DIR_PERM)
                this.out.putArchiveEntry(parentEntry)
                this.out.closeArchiveEntry()
            }
        }

        fun finish() {

        }
    }
}