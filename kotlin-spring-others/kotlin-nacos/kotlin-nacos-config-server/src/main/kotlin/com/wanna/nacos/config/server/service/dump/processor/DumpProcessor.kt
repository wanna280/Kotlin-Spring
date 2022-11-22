package com.wanna.nacos.config.server.service.dump.processor

import com.wanna.nacos.api.common.Constants
import com.wanna.nacos.api.common.task.NacosTask
import com.wanna.nacos.api.common.task.NacosTaskProcessor
import com.wanna.nacos.config.server.model.event.ConfigDumpEvent
import com.wanna.nacos.config.server.service.dump.DumpConfigHandler
import com.wanna.nacos.config.server.service.dump.DumpService
import com.wanna.nacos.config.server.service.dump.task.DumpTask
import com.wanna.nacos.config.server.utils.GroupKey2

/**
 * 执行Dump的Processor, 当[DumpTask]任务到来时, 就需要自动触发一下[ConfigDumpEvent]事件, 去进行一次dump
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/22
 *
 * @param dumpService DumpService
 * @see DumpTask
 */
open class DumpProcessor(private val dumpService: DumpService) : NacosTaskProcessor {

    override fun process(task: NacosTask): Boolean {
        if (task is DumpTask) {
            val persistService = dumpService.persistService
            val key = GroupKey2.parseKey(task.groupKey)
            val tenant = key[0]
            val group = key[1]
            val dataId = key[2]
            val configInfo = persistService.findConfigInfo(dataId, group, tenant)

            DumpConfigHandler.configDump(
                ConfigDumpEvent(
                    configInfo == null,
                    configInfo?.tenant ?: "",
                    configInfo?.dataId ?: "",
                    configInfo?.group ?: "",
                    configInfo?.content ?: "",
                    task.handleIp,
                    configInfo?.type ?: Constants.DEFAULT_CONFIG_TYPE,
                    task.lastModified
                )
            )
        }
        return true
    }
}