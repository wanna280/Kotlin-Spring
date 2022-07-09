package com.wanna.boot.devtools.restart.classloader

import com.wanna.framework.lang.Nullable
import java.io.Serializable

/**
 * ClassLoaderFileRepository的默认实现，维护了许多的ClassLoaderFile
 *
 * @see ClassLoaderFile
 * @see ClassLoaderFileRepository
 *
 * @param sourceDirectories 当前ClassLoaderFiles当中需要去进行维护的文件夹列表
 */
open class ClassLoaderFiles(private val sourceDirectories: MutableMap<String, SourceDirectory>) :
    ClassLoaderFileRepository {

    // 提供一个copy的方法
    constructor(classLoaderFiles: ClassLoaderFiles) : this(LinkedHashMap(classLoaderFiles.sourceDirectories))

    // 提供一个无参数构造器
    constructor() : this(LinkedHashMap())

    /**
     * 根据name去获取到对应的ClassLoaderFile
     *
     * @param name name
     * @return 寻找到的ClassLoaderFile(没有找到return null)
     */
    @Nullable
    override fun getFile(name: String): ClassLoaderFile? {
        sourceDirectories.values.forEach {
            val classLoaderFile = it.get(name)
            if (classLoaderFile != null) {
                return classLoaderFile
            }
        }
        return null
    }

    /**
     * 添加ClassLoaderFiles
     *
     * @param classLoaderFiles 你想要添加的ClassLoaderFiles
     */
    open fun addAll(classLoaderFiles: ClassLoaderFiles) {

    }

    class SourceDirectory : Serializable {
        fun get(name: String): ClassLoaderFile? {
            return null
        }
    }
}