package com.wanna.boot.devtools.classpath

import com.wanna.boot.devtools.filewatch.ChangedFiles
import com.wanna.framework.context.event.ApplicationEvent

/**
 * ClassPath当中的文件发生改变的事件
 *
 * @param source eventSource
 * @param changeSet 该事件发布时, 发生改变的文件列表(每个元素维护一个目录的文件信息)
 * @param restartRequired 指定的文件发生变更时, 是否应该去进行restart?
 */
open class ClassPathChangedEvent(source: Any, val changeSet: Set<ChangedFiles>, val restartRequired: Boolean) :
    ApplicationEvent(source)