package com.wanna.boot.devtools.classpath

import com.wanna.boot.devtools.filewatch.ChangedFile

interface ClassPathRestartStrategy {
    fun isRestartRequired(changedFile: ChangedFile): Boolean
}