package com.wanna.boot.devtools.restart.classloader

import com.wanna.framework.lang.Nullable
import java.io.Serializable

/**
 * ClassLoaderFileRepository的默认实现, 维护了许多的ClassLoaderFile; 
 * 为RestartClassLoader所服务, 用于去完成资源的加载(比如从远程加载一个文件)
 *
 * @see ClassLoaderFile
 * @see ClassLoaderFileRepository
 * @see RestartClassLoader
 *
 * @param sourceDirectories 当前ClassLoaderFiles当中需要去进行维护的文件夹列表
 */
open class ClassLoaderFiles(private val sourceDirectories: MutableMap<String, SourceDirectory>) :
    ClassLoaderFileRepository, Serializable {

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
        classLoaderFiles.sourceDirectories.forEach { (directoryName, directory) ->
            directory.getFileEntrySet().forEach { (fileName, file) ->
                addFile(directoryName, fileName, file)
            }
        }
    }

    /**
     * 添加一个ClassLoaderFile
     *
     * @param sourceDirectory sourceDirectoryName
     * @param name fileName
     * @param file 要添加的文件
     */
    open fun addFile(sourceDirectory: String, name: String, file: ClassLoaderFile) {
        getOrCreateSourceDirectory(sourceDirectory).add(name, file)
    }

    /**
     * 如果必要的话, 创建一个SourceDirectory
     *
     * @param sourceDirectory sourceDirectoryName
     * @return 创建/获取到的SourceDirectory
     */
    protected fun getOrCreateSourceDirectory(sourceDirectory: String): SourceDirectory {
        var directory = sourceDirectories[sourceDirectory]
        if (directory == null) {
            directory = SourceDirectory(sourceDirectory)
            this.sourceDirectories[sourceDirectory] = directory
        }
        return directory
    }

    /**
     * 描述了一个源文件夹下的文件信息
     *
     * @param name name, 文件夹的名称
     */
    class SourceDirectory(val name: String) : Serializable {
        private val files = LinkedHashMap<String, ClassLoaderFile>()

        /**
         * 根据fileName去获取到对应的ClassLoaderFile
         *
         * @param name fileName
         * @return 根据fileName寻找到的ClassLoaderFile(如果没有, 那么return null)
         */
        @Nullable
        fun get(name: String): ClassLoaderFile? = this.files[name]

        /**
         * 添加一个文件到当前的SourceDirectory当中
         *
         * @param name fileName
         * @param file 要添加的File
         */
        fun add(name: String, file: ClassLoaderFile) {
            this.files[name] = file
        }

        /**
         * 获取FileEntrySet, key-fileName, value-ClassLoaderFile
         *
         * @return FileEntrySet
         */
        fun getFileEntrySet(): Set<Map.Entry<String, ClassLoaderFile>> = LinkedHashSet(this.files.entries)

        /**
         * 获取所有的维护的文件列表
         *
         * @return 文件列表
         */
        fun getFiles(): Collection<ClassLoaderFile> = LinkedHashSet(this.files.values)
    }
}