package com.wanna.boot.devtools.classpath

import com.wanna.boot.devtools.filewatch.ChangedFiles
import com.wanna.boot.devtools.filewatch.FileChangeListener
import com.wanna.boot.devtools.filewatch.FileSystemWatcher
import com.wanna.framework.context.event.ApplicationEventPublisher
import com.wanna.framework.lang.Nullable

/**
 * ClassPath下的文件变更监听器, 负责使用Spring的EventPublisher去完成事件的发布,
 * 告知所有的处理ClassPath已经发生改变的事件的监听器, 来处理这个事件
 *
 * @param eventPublisher Spring ApplicationContext的EventPublisher, 用于进行事件的发布
 * @param restartStrategy 重启的策略(符合这个策略的要求的情况下, 才去进行重启)
 * @param fileSystemWatcherToStop 要去进行关闭的Watcher(可以为Null)
 */
open class ClassPathFileChangeListener(
    private val eventPublisher: ApplicationEventPublisher,
    private val restartStrategy: ClassPathRestartStrategy,
    @Nullable private val fileSystemWatcherToStop: FileSystemWatcher?
) : FileChangeListener {

    /**
     * 当ClassPath下的文件发生变更时, 应该执行的回调,
     * 应该使用EventPublisher去发布ClassPathChangedEvent
     *
     * @param changeSet 发生改变的文件情况
     */
    override fun onChange(changeSet: Set<ChangedFiles>) {
        // 遍历所有的已经改变的文件, 去判断是否需要去进行重启
        val restartRequired = isRestartRequired(changeSet)

        publishEvent(ClassPathChangedEvent(this, changeSet, restartRequired))
    }

    /**
     * 将给定的[event]利用[ApplicationEventPublisher]去进行发布, 并关闭[FileSystemWatcher]
     *
     * @param event event
     */
    private fun publishEvent(event: ClassPathChangedEvent) {
        this.eventPublisher.publishEvent(event)

        // 如果需要去进行重启, 那么需要将之前的Watcher去进行关闭
        if (event.restartRequired && this.fileSystemWatcherToStop != null) {
            this.fileSystemWatcherToStop.stop()
        }
    }

    /**
     * 根据给定的发生变更的文件列表, 去进行决策是否需要去进行重启?
     *
     * @param changeSet 发生改变的文件列表
     * @return 如果需要重启, return true; 否则return false
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