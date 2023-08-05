package com.wanna.framework.core.io

import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ResourceUtils
import com.wanna.framework.util.StringUtils
import java.io.*
import java.net.URI
import java.net.URISyntaxException
import java.net.URL
import java.nio.channels.FileChannel
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * FileSystem的Resource实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/9
 *
 * @param file File
 * @param path path
 * @param filePath FilePath
 */
open class FileSystemResource private constructor(
    @Nullable private val file: File?,
    val path: String,
    private val filePath: Path
) : AbstractResource(), WritableResource {

    /**
     * 基于文件路径去创建[FileSystemResource]
     *
     * @param path path
     */
    constructor(path: String) : this(File(path), StringUtils.cleanPath(path), File(path).toPath())

    /**
     * 基于一个明确给定的File去创建[FileSystemResource]
     *
     * @param file File
     */
    constructor(file: File) : this(file, StringUtils.cleanPath(file.path), file.toPath())

    /**
     * 基于一个[Path]去创建[FileSystemResource]
     *
     * @param filePath File Path
     */
    constructor(filePath: Path) : this(null, StringUtils.cleanPath(filePath.toString()), filePath)

    /**
     * 当前文件资源是否存在?
     *
     * @return 如果该文件存在, 那么return true; 否则return false
     */
    override fun exists(): Boolean = this.file?.exists() ?: Files.exists(this.filePath)

    /**
     * 获取到当前文件的输入流[InputStream]
     *
     * @return InputStream
     * @throws FileNotFoundException 如果该文件路径不存在
     * @see Files.newInputStream
     */
    @Throws(FileNotFoundException::class)
    override fun getInputStream(): InputStream {
        try {
            return Files.newInputStream(this.filePath)
        } catch (ex: NoSuchFileException) {
            throw FileNotFoundException(ex.message)
        }
    }

    /**
     * 检查当前文件资源是否可写?
     *
     * @return 如果当前不是一个文件夹, 并且可以写, 那么return true; 否则return false
     */
    override fun isWritable(): Boolean {
        if (this.file != null) {
            return !file.isDirectory && file.canWrite()
        }
        return !Files.isDirectory(this.filePath) && Files.isWritable(this.filePath)
    }

    /**
     * 当前文件资源是否可读?
     *
     * @return 如果文件可读, return true; 否则return false
     */
    override fun isReadable(): Boolean = this.file?.canRead() ?: Files.isReadable(this.filePath)

    /**
     * 根据FilePath, 去获取到该文件的[OutputStream]输出流
     *
     * @return OutputStream
     *
     * @see Files.newOutputStream
     */
    override fun getOutputStream(): OutputStream = Files.newOutputStream(this.filePath)

    /**
     * 根据FilePath, 获取到该文件的URL
     *
     * @return URL
     */
    override fun getURL(): URL = this.file?.toURI()?.toURL() ?: this.filePath.toUri().toURL()

    /**
     * 用于去返回当前文件的URI
     *
     * @return 当前文件的URI
     *
     * @see File.toURI
     * @see Path.toUri
     */
    override fun getURI(): URI {
        if (this.file != null) {
            return this.file.toURI()
        } else {
            var uri = this.filePath.toUri()

            // normalize uri
            if (ResourceUtils.URL_PROTOCOL_FILE == uri.scheme) {
                try {
                    uri = URI(uri.scheme, uri.path, null)
                } catch (ex: URISyntaxException) {
                    throw IOException("Failed to normalize URI: $uri", ex)
                }
            }
            return uri
        }
    }

    /**
     * 当前资源一定是文件, return true
     *
     * @return true
     */
    override fun isFile(): Boolean = true

    /**
     * 获取到当前资源对应的文件
     *
     * @return File
     */
    override fun getFile(): File = this.file ?: this.filePath.toFile()


    /**
     * 返回当前文件的读通道
     *
     * @return 当前文件的读通道[ReadableByteChannel]
     */
    override fun readableChannel(): ReadableByteChannel = FileChannel.open(this.filePath, StandardOpenOption.READ)

    /**
     * 返回当前文件的写通道
     *
     * @return 当前文件的写通道[WritableByteChannel]
     */
    override fun writableChannel(): WritableByteChannel = FileChannel.open(this.filePath, StandardOpenOption.WRITE)

    /**
     * 返回当前文件的文件名
     *
     * @return fileName
     */
    override fun getFilename(): String = this.file?.name ?: this.filePath.fileName.toString()

    /**
     * 获取当前资源的描述信息
     *
     * @return description
     */
    override fun getDescription(): String = "file [${file?.absolutePath ?: this.filePath.toAbsolutePath()}]"

    /**
     * equals方法的生成, 使用path字段去进行比较
     *
     * @param other other
     */
    override fun equals(@Nullable other: Any?): Boolean =
        this === other || other is FileSystemResource && this.path == other.path

    /**
     * hashCode方法的生成, 使用path字段去生成
     *
     * @return hashCode
     */
    override fun hashCode(): Int = this.path.hashCode()
}