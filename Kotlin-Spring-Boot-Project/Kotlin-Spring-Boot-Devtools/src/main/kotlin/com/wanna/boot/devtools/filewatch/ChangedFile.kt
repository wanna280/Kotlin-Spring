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
     * 文件发生改变的类型的枚举值
     */
    enum class Type { ADD, DELETE, MODIFY }
}