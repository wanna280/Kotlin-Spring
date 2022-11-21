package com.wanna.nacos.config.server.service.dump.task

import com.wanna.nacos.api.common.task.AbstractDelayTask

/**
 * 用于进行Dump的延时任务
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 *
 * @param groupKey "dataId"/"group"/"tenant"
 * @param lastModified 上次修改时间
 * @param handleIp handleIp
 * @param tag tag
 */
open class DumpTask(val groupKey: String, val lastModified: Long, val handleIp: String, val tag: String = "") :
    AbstractDelayTask() {
    init {
        // 设置任务的执行时间间隔为1s
        this.taskInterval = 1000L
    }
}