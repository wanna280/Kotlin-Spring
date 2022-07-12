package com.wanna.boot.devtools.filewatch

import java.io.File

/**
 * 记录一个已经发生改变的文件的具体信息
 *
 * @param sourceDirectory 文件所在目录
 * @param file 发生改变的文件
 * @param type 文件改变的类型(ADD/DELETE/UPDATE)
 */
class ChangedFile(val sourceDirectory: File, val file: File, val type: Type) {

    /**
     * 获取"file"相对于"sourceDirectory"的相对路径，有可能并不是只相差了一级目录，有可能相差了多级的目录
     *
     * @return relativeName
     */
    fun getRelativeName(): String {
        val directoryPath = sourceDirectory.absoluteFile.path
        val filePath = file.absoluteFile.path
        return filePath.substring(directoryPath.length + 1)
    }

    /**
     * 文件发生改变的类型的枚举值
     */
    enum class Type { ADD, DELETE, MODIFY }
}