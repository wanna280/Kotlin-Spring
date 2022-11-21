package com.wanna.nacos.config.server.service.dump

import com.wanna.nacos.api.notify.Event
import com.wanna.nacos.api.notify.listener.Subscriber
import com.wanna.nacos.config.server.model.event.ConfigDumpEvent
import com.wanna.nacos.config.server.service.ConfigCacheService

/**
 * 处理[ConfigDumpEvent]事件的[Subscriber]处理器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/22
 */
open class DumpConfigHandler : Subscriber<ConfigDumpEvent>() {

    companion object {

        /**
         * 处理[ConfigDumpEvent]事件, 使用[ConfigCacheService]去进行真正的dump
         *
         * @param event ConfigDumpEvent
         */
        @JvmStatic
        fun configDump(event: ConfigDumpEvent) {
            // 如果是配置文件发生了移除... 那么需要从ConfigCacheService当中去移除掉
            if (event.remove) {
                ConfigCacheService
                    .remove(event.dataId, event.group, event.tenant)

                // 如果是配置文件发生了变更... 那么需要执行一次dump
            } else {
                ConfigCacheService
                    .dump(event.dataId, event.group, event.tenant, event.content, event.lastModifiedTs, event.type)
            }
        }
    }


    override fun onEvent(event: ConfigDumpEvent) {
        configDump(event)
    }

    override fun subscribeType(): Class<out Event> = ConfigDumpEvent::class.java
}