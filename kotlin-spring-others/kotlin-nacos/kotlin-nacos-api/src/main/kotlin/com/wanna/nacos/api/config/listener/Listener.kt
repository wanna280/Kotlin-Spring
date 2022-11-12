package com.wanna.nacos.api.config.listener

import java.util.concurrent.Executor

/**
 * Nacos Config的Listener, 监听配置文件的变化
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/12
 */
interface Listener {

    /**
     * 获取用于执行当前Listener任务的Executor线程池
     *
     * @return Executor
     */
    fun getExecutor(): Executor

    /**
     * 接收配置信息并进行处理
     *
     * @param configInfo configInfo
     */
    fun receiveConfigInfo(configInfo: String)
}