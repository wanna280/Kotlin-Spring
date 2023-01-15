package com.wanna.boot.devtools.filewatch

/**
 * FileSystemWatcher的Factory, 负责提供FileSystemWatcher的生产
 *
 * @see FileSystemWatcher
 */
@FunctionalInterface
interface FileSystemWatcherFactory {
    fun getFileSystemWatcher(): FileSystemWatcher
}