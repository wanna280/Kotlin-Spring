package com.wanna.nacos.config.server.service.dump.processor

import com.wanna.nacos.api.common.task.NacosTask
import com.wanna.nacos.api.common.task.NacosTaskProcessor
import com.wanna.nacos.config.server.service.ConfigCacheService
import com.wanna.nacos.config.server.service.dump.DumpService
import com.wanna.nacos.config.server.service.repository.PersistService

/**
 * DumpAll的Processor
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 */
class DumpAllProcessor(private val dumpService: DumpService) : NacosTaskProcessor {

    /**
     * PersistService
     */
    private val persistService: PersistService = dumpService.persistService

    override fun process(task: NacosTask): Boolean {
        val listAllGroupKeyMd5 = persistService.listAllGroupKeyMd5()
        listAllGroupKeyMd5.forEach {
            ConfigCacheService.dump(
                it.dataId ?: "",
                it.group ?: "",
                it.tenant ?: "",
                it.content ?: "",
                it.lastModified,
                it.type ?: ""
            )
        }
        return true
    }
}