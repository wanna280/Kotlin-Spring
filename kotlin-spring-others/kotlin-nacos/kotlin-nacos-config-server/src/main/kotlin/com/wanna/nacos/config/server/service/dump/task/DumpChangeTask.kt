package com.wanna.nacos.config.server.service.dump.task

import com.wanna.nacos.api.common.task.AbstractDelayTask
import com.wanna.nacos.config.server.service.dump.DumpService

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 */
class DumpChangeTask(private val dumpService: DumpService) : AbstractDelayTask() {

}