package com.wanna.boot.devtools.autoconfigure

import java.io.File
import java.io.FileFilter

/**
 * 触发文件的Filter
 *
 * @param name 指定的要去进行触发的文件名
 */
class TriggerFileFilter(private val name: String) : FileFilter {
    override fun accept(pathname: File?): Boolean = pathname?.name == name
}