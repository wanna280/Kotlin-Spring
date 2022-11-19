package com.wanna.nacos.config.server.service.dump.processor

import com.wanna.nacos.api.common.task.NacosTask
import com.wanna.nacos.api.common.task.NacosTaskProcessor
import com.wanna.nacos.config.server.service.ConfigCacheService
import com.wanna.nacos.config.server.service.dump.DumpService
import com.wanna.nacos.config.server.utils.GroupKey2

/**
 * Dump配置文件变更情况的处理器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 */
class DumpChangeProcessor(dumpService: DumpService) : NacosTaskProcessor {

    /**
     * PersistService
     */
    private val persistService = dumpService.getPersistService()

    override fun process(task: NacosTask): Boolean {
        // 从PersistService当中查出来所有的GroupKey和MD5信息, 去更新ConfigCacheService当中的所有的GroupKey的MD5
        val listAllGroupKeyMd5 = persistService.listAllGroupKeyMd5()
        listAllGroupKeyMd5.forEach {
            val groupKey = GroupKey2.getKeyTenant(it.dataId ?: "", it.group ?: "", it.tenant ?: "")

            // 更新ConfigCacheService的MD5值, 同时会触发LocalDataChangeEvent事件的发生...
            // 从而触发LongPollingService的Response的写入, 告知客户端配置文件已经发生了变更...
            ConfigCacheService.updateMd5(groupKey, it.md5 ?: "", it.lastModified)
        }

        ConfigCacheService.dumpChange("", "", "", "", 0L)
        return true
    }
}