package com.wanna.boot.devtools.filewatch

import java.io.File

/**
 * 描述指定的目录下的，内容发生改变的文件列表
 *
 * @param sourceDirectory 要去进行描述的目录(File)
 * @param files 该目录下发生改变的文件的列表
 */
class ChangedFiles(val sourceDirectory: File, val files: List<ChangedFile>) : Iterable<ChangedFile> {
    override fun iterator() = this.files.listIterator()
}