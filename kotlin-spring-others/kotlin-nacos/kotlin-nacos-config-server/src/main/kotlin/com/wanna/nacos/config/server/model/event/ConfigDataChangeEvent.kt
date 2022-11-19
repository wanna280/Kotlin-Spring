package com.wanna.nacos.config.server.model.event

import com.wanna.nacos.api.notify.Event

/**
 * ConfigData发生变化的事件
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 */
class ConfigDataChangeEvent(
    val dataId: String,
    val group: String,
    val tenant: String,
    val lastModifiedTs: Long,
    val tag: String = ""
) : Event()