package com.wanna.nacos.api.common.task

/**
 * Nacos任务的处理器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 */
fun interface NacosTaskProcessor {

    /**
     * 处理一个Nacos任务
     *
     * @param task NacosTask
     */
    fun process(task: NacosTask): Boolean
}