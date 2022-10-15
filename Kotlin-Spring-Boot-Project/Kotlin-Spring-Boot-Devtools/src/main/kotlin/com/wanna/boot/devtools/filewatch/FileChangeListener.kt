package com.wanna.boot.devtools.filewatch

/**
 * 文件发生改变的监听器，负责去监听文件的变化情况
 */
interface FileChangeListener {

    /**
     * 当文件发生变更时，会自动回调此方法去处理文件改变的事件
     *
     * @param changeSet 发生改变的文件列表
     */
    fun onChange(changeSet: Set<ChangedFiles>)
}