package com.wanna.boot.devtools.classpath

import com.wanna.boot.devtools.filewatch.ChangedFiles
import com.wanna.boot.devtools.filewatch.FileChangeListener
import com.wanna.boot.devtools.filewatch.FileSystemWatcher
import com.wanna.framework.context.event.ApplicationEventPublisher

/**
 * ClassPath下的文件变更监听器
 *
 * @param eventPublisher Spring的EventPublisher
 * @param restartStrategy 重启的策略(符合要求的才去进行重启)
 * @param fileSystemWatcherToStop 要去进行关闭的Watcher(可以为Null)
 */
class ClassPathFileChangeListener(
    private val eventPublisher: ApplicationEventPublisher,
    private val restartStrategy: ClassPathRestartStrategy,
    private val fileSystemWatcherToStop: FileSystemWatcher?
) : FileChangeListener {

    /**
     * 当ClassPath下的文件发生变更时，应该执行的回调，应该使用EventPublisher去发布ClassPathChangedEvent
     *
     * @param changeSet 发生改变的文件情况
     */
    override fun onChange(changeSet: Set<ChangedFiles>) {
        // 遍历所有的已经改变的文件，去判断是否需要去进行重启
        val restartRequired = isRestartRequired(changeSet)

        publishEvent(ClassPathChangedEvent(this, changeSet, restartRequired))
    }

    private fun publishEvent(event: ClassPathChangedEvent) {
        this.eventPublisher.publishEvent(event)
        if (event.restartRequired && this.fileSystemWatcherToStop != null) {
            this.fileSystemWatcherToStop.stop()
        }
    }

    /**
     * 决策是否需要去进行重启？
     *
     * @param changeSet 发生改变的文件的累彪
     * @return 如果需要重启，return true；否则return false
     */
    private fun isRestartRequired(changeSet: Set<ChangedFiles>): Boolean {
        changeSet.forEach { changedFiles ->
            changedFiles.forEach {
                if (restartStrategy.isRestartRequired(it)) {
                    return true
                }
            }
        }
        return false
    }
}