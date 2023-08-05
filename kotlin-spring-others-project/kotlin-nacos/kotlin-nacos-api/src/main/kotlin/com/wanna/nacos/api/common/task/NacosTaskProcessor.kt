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
     * 交给[NacosTask]的任务处理器去处理一个NacosTask任务
     *
     * @param task NacosTask
     * @return 处理当前NacosTask是否成功? 处理成功return true; 否则return false
     */
    fun process(task: NacosTask): Boolean
}