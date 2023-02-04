package com.wanna.debugger.bistoury.instrument.client.debugger

import com.wanna.debugger.bistoury.instrument.client.common.InstrumentInfo
import java.util.concurrent.atomic.AtomicBoolean
import javax.annotation.Nullable

/**
 * [BistouryDebugClient]的工厂对象
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 *
 * @see BistouryDebugClient
 */
object BistouryDebugClients {
    /**
     * 已经创建的[BistouryDebugClient]对象
     */
    @JvmStatic
    @Nullable
    private var client: BistouryDebugClient? = null

    /**
     * 单例[BistouryDebugClient]对象是否已经完成初始化的标志位
     */
    @JvmStatic
    private val inited = AtomicBoolean()

    /**
     * 获取之前已经创建的[BistouryDebugClient]
     *
     * @return BistouryDebugClient
     * @throws IllegalStateException 如果之前没完成BistouryDebugClient的创建和初始化
     */
    @Throws(IllegalStateException::class)
    @JvmStatic
    fun getInstance(): BistouryDebugClient {
        return client ?: throw IllegalStateException("BistouryDebugClient is not available")
    }

    /**
     * 创建[BistouryDebugClient]并将结果去进行缓存起来, 后续可以通过[getInstance]获取到
     *
     * @param instrumentInfo InstrumentInfo
     * @return BistouryDebugClient
     * @throws IllegalStateException 如果之前BistouryDebugClient已经完成过创建
     */
    @Throws(IllegalStateException::class)
    @JvmStatic
    fun create(instrumentInfo: InstrumentInfo): BistouryDebugClient {
        if (!inited.compareAndSet(false, true)) {
            throw IllegalStateException("BistouryDebugClient already exists")
        }
        val bistouryDebugClient = BistouryDebugClient(instrumentInfo)
        this.client = bistouryDebugClient
        return bistouryDebugClient
    }
}