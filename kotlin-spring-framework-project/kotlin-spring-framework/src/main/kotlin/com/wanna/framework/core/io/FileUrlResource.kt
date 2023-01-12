package com.wanna.framework.core.io

import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ResourceUtils
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.nio.file.Files

/**
 * 基于File的UrlResource，是一类特殊的UrlResource(protocol=file)
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/1
 * @see UrlResource
 * @param path 文件的路径
 */
open class FileUrlResource(path: String) : UrlResource(ResourceUtils.URL_PROTOCOL_FILE, path),
    WritableResource {
    constructor(url: URL) : this(url.path)

    @Nullable
    @Volatile
    private var file: File? = null

    override fun getFile(): File {
        var file = this.file
        if (file != null) {
            return file
        }
        file = super.getFile()
        this.file = file
        return file
    }

    /**
     * 获取InputStream
     *
     * @return InputStream
     */
    override fun getInputStream(): InputStream = Files.newInputStream(getFile().toPath())

    override fun isWritable(): Boolean {
        return try {
            val file = getFile()
            file.canWrite() && !file.isDirectory
        } catch (ex: IOException) {
            false
        }
    }

    override fun getOutputStream(): OutputStream = Files.newOutputStream(getFile().toPath())
}