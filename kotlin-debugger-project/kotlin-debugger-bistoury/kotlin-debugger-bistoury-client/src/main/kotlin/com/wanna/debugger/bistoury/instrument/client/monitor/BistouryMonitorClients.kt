package com.wanna.debugger.bistoury.instrument.client.monitor

import com.wanna.debugger.bistoury.instrument.client.common.InstrumentInfo
import java.util.concurrent.atomic.AtomicBoolean
import javax.annotation.Nullable

/**
 * [BistouryMonitorClient]的工厂对象
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/31
 */
object BistouryMonitorClients {
    /**
     * 已经创建的[BistouryMonitorClient]对象
     */
    @JvmStatic
    @Nullable
    private var client: BistouryMonitorClient? = null

    /**
     * 是否已经完成初始化的标志位
     */
    @JvmStatic
    private val inited = AtomicBoolean()

    /**
     * 获取之间已经创建的[BistouryMonitorClient]
     *
     * @return BistouryMonitorClient
     * @throws IllegalStateException 如果之前没完成BistouryMonitorClient的创建
     */
    @Throws(IllegalStateException::class)
    @JvmStatic
    fun getInstance(): BistouryMonitorClient {
        return client ?: throw IllegalStateException("BistouryMonitorClient is not available")
    }

    /**
     * 创建[BistouryMonitorClient]并将结果去进行缓存起来, 后续可以通过[getInstance]获取到
     *
     * @param instrumentInfo InstrumentInfo
     * @return BistouryMonitorClient
     * @throws IllegalStateException 如果之前BistouryMonitorClient已经完成过创建
     */
    @Throws(IllegalStateException::class)
    @JvmStatic
    fun create(instrumentInfo: InstrumentInfo): BistouryMonitorClient {
        if (!inited.compareAndSet(false, true)) {
            throw IllegalStateException("BistouryMonitorClient already exists")
        }
        val bistouryDebugClient = BistouryMonitorClient(instrumentInfo)
        this.client = bistouryDebugClient
        return bistouryDebugClient
    }
}