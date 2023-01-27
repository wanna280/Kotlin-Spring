package com.wanna.boot.devtools.restart.classloader

import com.wanna.framework.lang.Nullable
import java.io.Serializable

/**
 * [ClassLoaderFileRepository]的默认实现, 维护了许多的ClassLoaderFile;
 * 为[RestartClassLoader]所服务, 用于去完成资源的加载(比如从远程加载一个文件)
 *
 * @see ClassLoaderFile
 * @see ClassLoaderFileRepository
 * @see RestartClassLoader
 *
 * @param sourceDirectories 当前ClassLoaderFiles当中需要去进行维护的文件夹列表(包含该文件夹下的所有文件列表)
 */
open class ClassLoaderFiles(private val sourceDirectories: MutableMap<String, SourceDirectory>) :
    ClassLoaderFileRepository, Serializable {

    /**
     * 提供一个进行copy的方法, 将另外一个[ClassLoaderFiles]当中的内容copy过来
     *
     * @param classLoaderFiles origin ClassLoaderFiles
     */
    constructor(classLoaderFiles: ClassLoaderFiles) : this(LinkedHashMap(classLoaderFiles.sourceDirectories))

    /**
     * 提供一个无参数构造器
     */
    constructor() : this(LinkedHashMap())

    /**
     * 根据文件名去获取到对应的[ClassLoaderFile]文件
     *
     * @param name 文件名(例如"com/wanna/App.class")
     * @return 寻找到的ClassLoaderFile(没有找到return null)
     */
    @Nullable
    override fun getFile(name: String): ClassLoaderFile? {
        sourceDirectories.values.forEach {
            val classLoaderFile = it[name]
            if (classLoaderFile != null) {
                return classLoaderFile
            }
        }
        return null
    }

    /**
     * 将另外一个[ClassLoaderFiles]当中的所有的[SourceDirectory]信息去merge到当前的[ClassLoaderFiles]当中
     *
     * @param classLoaderFiles ClassLoaderFiles to add
     */
    open fun addAll(classLoaderFiles: ClassLoaderFiles) {
        classLoaderFiles.sourceDirectories.forEach { (directoryName, directory) ->
            directory.getFileEntrySet().forEach { (fileName, file) ->
                addFile(directoryName, fileName, file)
            }
        }
    }

    /**
     * 添加一个[ClassLoaderFile]到当前[ClassLoaderFiles]当中来
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
    protected open fun getOrCreateSourceDirectory(sourceDirectory: String): SourceDirectory {
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
     * @param name 文件夹的名称
     */
    class SourceDirectory(val name: String) : Serializable {

        /**
         * 该文件夹下的文件列表
         */
        private val files = LinkedHashMap<String, ClassLoaderFile>()

        /**
         * 根据fileName去获取到对应的ClassLoaderFile
         *
         * @param name fileName
         * @return 根据fileName寻找到的ClassLoaderFile(如果没有, 那么return null)
         */
        @Nullable
        operator fun get(name: String): ClassLoaderFile? = this.files[name]

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
         * 获取当前[SourceDirectory]当中维护的所有的文件列表
         *
         * @return 文件列表
         */
        fun getFiles(): Collection<ClassLoaderFile> = LinkedHashSet(this.files.values)
    }
}