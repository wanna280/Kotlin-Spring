package com.wanna.boot.devtools.classpath

import com.wanna.boot.devtools.filewatch.ChangedFile

/**
 * ClassPath下的配置文件发生变更的策略接口, 检查某个文件发生变更时, 是否需要去进行重新?
 *
 * @see PatternClassPathRestartStrategy
 */
interface ClassPathRestartStrategy {

    /**
     * 当[changedFile]这个文件发生变更时, 是否需要去进行重启?
     *
     * @param changedFile ChangedFile, 发生变更的文件信息
     * @return 是否需要去执行重新启动?
     */
    fun isRestartRequired(changedFile: ChangedFile): Boolean
}