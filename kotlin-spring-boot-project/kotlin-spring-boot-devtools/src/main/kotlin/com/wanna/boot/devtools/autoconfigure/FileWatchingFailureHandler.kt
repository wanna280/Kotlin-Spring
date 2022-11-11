package com.wanna.boot.devtools.autoconfigure

import com.wanna.boot.devtools.filewatch.FileSystemWatcherFactory
import com.wanna.boot.devtools.restart.FailureHandler

/**
 * "FileWatching"过程当中出现的异常的处理器
 *
 * @see FailureHandler
 * @see FileSystemWatcherFactory
 */
open class FileWatchingFailureHandler(private val factory: FileSystemWatcherFactory) : FailureHandler {
    override fun handle(error: Throwable): FailureHandler.Outcome {
        return FailureHandler.Outcome.RETRY
    }
}