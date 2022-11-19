package com.wanna.nacos.config.server.service.dump.processor

import com.wanna.nacos.api.common.task.NacosTask
import com.wanna.nacos.api.common.task.NacosTaskProcessor
import com.wanna.nacos.config.server.service.dump.DumpService

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 */
class DumpAllProcessor(private val dumpService: DumpService) : NacosTaskProcessor {

    override fun process(task: NacosTask): Boolean {
        return true
    }
}