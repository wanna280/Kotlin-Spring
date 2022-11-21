package com.wanna.nacos.config.server.model.event

import com.wanna.nacos.api.notify.Event
import com.wanna.nacos.config.server.service.notify.AsyncNotifyService

/**
 * ConfigData发生变化的事件
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 *
 * @see AsyncNotifyService
 */
open class ConfigDataChangeEvent(
    val dataId: String,
    val group: String,
    val tenant: String,
    val lastModifiedTs: Long,
    val tag: String = ""
) : Event()