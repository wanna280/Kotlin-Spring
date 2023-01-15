package com.wanna.boot.autoconfigure

import java.util.EventObject

/**
 * 这是一个AutoConfiguration的ImportEvent, 当SpringBoot完成自动配置之后就会发布这个事件
 *
 * @param source EventSource
 * @param configurations 自动配置类列表
 * @param excludes 排除掉的配置类列表
 * @see AutoConfigurationImportListener
 */
open class AutoConfigurationImportEvent(
    source: Any, val configurations: MutableList<String>, val excludes: Set<String>
) : EventObject(source)