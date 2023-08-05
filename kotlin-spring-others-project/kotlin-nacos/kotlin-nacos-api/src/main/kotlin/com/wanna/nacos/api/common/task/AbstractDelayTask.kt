package com.wanna.nacos.api.common.task

/**
 * 抽象的延时任务的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 */
abstract class AbstractDelayTask : NacosTask {

    /**
     * 任务的执行时间间隔
     */
    var taskInterval: Long = 0L

    /**
     * 任务的上次执行时间
     */
    var lastProcessTime: Long = 0L

    override fun shouldProcess(): Boolean = System.currentTimeMillis() - lastProcessTime > taskInterval
}