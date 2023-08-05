package com.wanna.boot.devtools.restart.classloader

import java.io.Serializable

/**
 * 为RestartClassLoader服务, 描述了一个为RestartClassLoader所使用的一个文件; 
 * 对于ClassLoaderFile的来源, 可以是来自于网络, 也可以是来自于别的地方
 *
 * @see RestartClassLoader
 * @see ClassLoaderFiles
 * @see ClassLoaderFileRepository
 *
 * @param contents 文件当中的内容进行序列化之后的结果(ByteArray)
 * @param kind 文件发生变更的类型(ADDED/DELETED/MODIFIED)
 * @param lastModified 该文件的最后一次修改的时间
 */
open class ClassLoaderFile(val contents: ByteArray, val kind: Kind, val lastModified: Long) : Serializable {
    /**
     * 对ClassLoaderFile文件的操作的类型的枚举, 包括加/删/改三种类型的操作
     */
    enum class Kind { ADDED, DELETED, MODIFIED }
}