package com.wanna.nacos.config.server.service.dump.task

import com.wanna.nacos.api.common.task.AbstractDelayTask

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 */
class DumpAllTask : AbstractDelayTask() {

    companion object {
        /**
         * DumpAll任务的TaskId
         */
        const val TASK_ID = "dumpAllConfigTask"
    }
}