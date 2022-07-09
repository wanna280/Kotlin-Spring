package com.wanna.boot.devtools.restart.classloader

import java.io.Serializable

/**
 * 为RestartClassLoader服务，描述了一个为RestartClassLoader所使用的一个文件
 *
 * @see RestartClassLoader
 */
open class ClassLoaderFile(val content: ByteArray, val kind: Kind, val lastModified: Long) : Serializable {
    /**
     * 对ClassLoaderFile文件的操作的类型的枚举，包括加/删/改三种类型的操作
     */
    enum class Kind { ADDED, DELETED, MODIFIED }
}