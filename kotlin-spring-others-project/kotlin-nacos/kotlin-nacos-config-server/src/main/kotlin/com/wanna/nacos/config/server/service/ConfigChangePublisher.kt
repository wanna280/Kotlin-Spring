package com.wanna.nacos.config.server.service

import com.wanna.nacos.api.notify.NotifyCenter
import com.wanna.nacos.config.server.model.event.ConfigDataChangeEvent

/**
 * ConfigData发生变更的事件发布器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 */
object ConfigChangePublisher {

    /**
     * 通知所有的Listener, 配置文件已经发生了变更
     *
     * @param event event
     */
    @JvmStatic
    fun notifyConfigChange(event: ConfigDataChangeEvent) {
        NotifyCenter.publishEvent(event)
    }
}