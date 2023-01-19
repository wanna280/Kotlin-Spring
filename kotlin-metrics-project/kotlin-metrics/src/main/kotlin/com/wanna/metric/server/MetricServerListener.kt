package com.wanna.metric.server

/**
 * 监听[MetricServer]的生命周期的Listener, 基于SPI去提供扩展
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/18
 *
 * @see MetricServer
 */
interface MetricServerListener {

    /**
     * 获取当前Listener的优先级, 数字越小优先级越高
     *
     * @return order
     */
    fun getOrder(): Int = 0

    /**
     * 监听对于[MetricServer]的初始化, 当[MetricServer]启动之前会自动触发
     *
     * @param server MetricServer
     * @see MetricServer.start
     */
    fun init(server: MetricServer) {}

    /**
     * 监听对于[MetricServer]的关闭, 当[MetricServer]关闭之前会自动触发完成收尾工作
     *
     * @param server MetricServer
     * @see MetricServer.stop
     */
    fun destroy(server: MetricServer) {}
}