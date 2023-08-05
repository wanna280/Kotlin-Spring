package com.wanna.debugger.bistoury.instrument.client.monitor

import com.wanna.metric.Metrics

/**
 * Bistoury AgentMonitor, 用于去进行监控指标的记录, 会被ASM生成字节码使用反射的方式去去进行回调这个类当中的方法
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/1
 */
object BistouryAgentMonitor {

    /**
     * 开始记录指标, 获取到当前时间的时间戳
     *
     * @return 当前时间戳
     */
    @JvmStatic
    fun start(): Long {
        return System.currentTimeMillis()
    }

    /**
     * 结束指标的记录
     *
     * @param key 指标Key
     * @param startTime 指标的开始时间
     */
    @JvmStatic
    fun stop(key: String, startTime: Long) {
        if (startTime != 0L && key.isNotBlank()) {
            Metrics.recordOne(key, System.currentTimeMillis() - startTime)
        }
    }

    /**
     * 记录异常指标
     *
     * @param key 指标Key
     */
    @JvmStatic
    fun exception(key: String) {
        Metrics.recordOne(key + "_exception")
    }
}