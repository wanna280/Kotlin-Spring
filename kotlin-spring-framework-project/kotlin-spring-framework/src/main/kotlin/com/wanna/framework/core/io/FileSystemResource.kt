package com.wanna.framework.core.io

import com.wanna.framework.util.StringUtils
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * FileSystem的Resource
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/9
 */
open class FileSystemResource(private val file: File) : AbstractResource(), WritableResource {

    private val path = StringUtils.cleanPath(file.path)

    override fun getInputStream(): InputStream = file.inputStream()

    override fun getDescription(): String? = "file [${file.absolutePath}]"

    override fun isWritable(): Boolean = file.canWrite()

    override fun getOutputStream(): OutputStream = file.outputStream()
}