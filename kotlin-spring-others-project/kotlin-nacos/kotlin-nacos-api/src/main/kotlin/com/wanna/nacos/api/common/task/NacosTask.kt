package com.wanna.nacos.api.common.task

/**
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/20
 */
interface NacosTask {

    /**
     * 该任务是否应该被处理的回调
     *
     * @return 如果应该被处理, return true; 否则return false
     */
    fun shouldProcess(): Boolean
}