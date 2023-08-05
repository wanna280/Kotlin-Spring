package com.wanna.nacos.config.server.model.event

import com.wanna.nacos.api.notify.Event

/**
 * ConfigServer本地的配置文件发生变更的事件
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/16
 *
 * @param groupKey 发生了变更的配置文件的groupKey(dataId&group&tenant)
 */
class LocalDataChangeEvent(val groupKey: String) : Event() {

}