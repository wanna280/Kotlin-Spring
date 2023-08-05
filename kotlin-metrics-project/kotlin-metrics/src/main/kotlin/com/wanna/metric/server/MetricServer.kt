package com.wanna.metric.server

/**
 * 暴露监控指标的Server
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 */
interface MetricServer {

    /**
     * 添加一个[MetricServerListener], 去监听[MetricServer]的启动和关闭
     *
     * @param listener Listener
     */
    fun addListener(listener: MetricServerListener)

    /**
     * 启动MetricServer
     */
    fun start()

    /**
     * 关闭MetricServer
     */
    fun stop()
}