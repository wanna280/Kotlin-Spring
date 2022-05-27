package com.wanna.boot.autoconfigure

import java.util.EventListener

/**
 * 这是一个AutoConfiguration的ImportListener，当SpringBoot的AutoConfiguration完成时，会被发布AutoConfigurationImportEvent事件从而被自动回调
 *
 * @see AutoConfigurationImportEvent
 */
interface AutoConfigurationImportListener : EventListener {
    fun onAutoConfigurationImportEvent(event: AutoConfigurationImportEvent)
}