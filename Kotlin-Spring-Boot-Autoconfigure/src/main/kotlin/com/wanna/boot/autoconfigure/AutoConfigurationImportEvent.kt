package com.wanna.boot.autoconfigure

import java.util.EventObject

/**
 * 这是一个AutoConfiguration的ImportEvent，当SpringBoot完成自动配置之后，会发布这个事件
 *
 * @see AutoConfigurationImportListener
 */
open class AutoConfigurationImportEvent(
    source: Any, val configurations: MutableList<String>, val excludes: Set<String>
) : EventObject(source) {

}