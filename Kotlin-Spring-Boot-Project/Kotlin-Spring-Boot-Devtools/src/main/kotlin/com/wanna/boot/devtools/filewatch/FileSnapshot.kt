package com.wanna.boot.devtools.filewatch

import java.io.File

/**
 * 维护了一个文件的Snapshot(快照)信息
 *
 * @param file 要去进行描述的文件
 */
open class FileSnapshot(val file: File) {
    // 文件的长度
    val length = file.length()

    // 上次修改时间
    val lastModified = file.lastModified()

    // 文件是否已经存在？
    val exists = file.exists()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as FileSnapshot
        if (file != other.file) return false
        if (length != other.length) return false
        if (lastModified != other.lastModified) return false
        if (exists != other.exists) return false
        return true
    }

    override fun hashCode(): Int {
        var result = file.hashCode()
        result = 31 * result + length.hashCode()
        result = 31 * result + lastModified.hashCode()
        result = 31 * result + exists.hashCode()
        return result
    }

    override fun toString() = this.file.toString()
}