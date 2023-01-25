package com.wanna.boot.devtools.filewatch

/**
 * [FileSystemWatcher]的Factory, 负责提供FileSystemWatcher的创建
 *
 * @see FileSystemWatcher
 */
@FunctionalInterface
fun interface FileSystemWatcherFactory {

    /**
     * 创建[FileSystemWatcher]的工厂方法
     *
     * @return FileSystemWatcher
     */
    fun getFileSystemWatcher(): FileSystemWatcher
}