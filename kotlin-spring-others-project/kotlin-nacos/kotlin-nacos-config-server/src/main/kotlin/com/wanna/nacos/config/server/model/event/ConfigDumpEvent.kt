package com.wanna.nacos.config.server.model.event

import com.wanna.nacos.api.notify.Event

/**
 * ConfigServer的配置文件需要去进行Dump的事件
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/22
 */
class ConfigDumpEvent(
    var remove: Boolean,
    var tenant: String,
    var dataId: String,
    var group: String,
    var content: String,
    var handleIp: String,
    var type: String,
    var lastModifiedTs: Long
) : Event() {

}