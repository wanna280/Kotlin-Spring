package com.wanna.debugger.bistoury.instrument.client.common

import com.wanna.debugger.bistoury.instrument.client.classpath.DefaultAppClassPathSupplier
import com.wanna.debugger.bistoury.instrument.client.classpath.DefaultAppLibClassSupplier
import com.wanna.debugger.bistoury.instrument.client.debugger.BistouryDebugClient
import com.wanna.debugger.bistoury.instrument.client.debugger.BistouryDebugClients
import com.wanna.debugger.bistoury.instrument.client.monitor.BistouryMonitorClient
import com.wanna.debugger.bistoury.instrument.client.monitor.BistouryMonitorClients
import java.lang.instrument.Instrumentation
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import javax.annotation.Nullable

/**
 * [InstrumentClient]的仓库, 负责启动各个[InstrumentClient]并进行保存
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 *
 * @see Instrumentation
 * @see InstrumentClient
 */
object InstrumentClientStore {

    /**
     * 记录是否被初始化过的标志位
     */
    @JvmStatic
    private val inited = AtomicBoolean()

    /**
     * 维护所有的[InstrumentClient], 比如DebugClient/MonitorClient
     */
    @JvmStatic
    private var instrumentClients: List<InstrumentClient> = emptyList()

    /**
     * Lock
     */
    @JvmStatic
    private val lock = ReentrantLock()

    /**
     * InstrumentInfo, 封装[Instrumentation]等信息
     */
    @JvmStatic
    @Nullable
    private var instrumentInfo: InstrumentInfo? = null

    /**
     * 利用[Instrumentation]去完成各个[InstrumentClient]的初始化
     *
     * @param instrumentation Instrumentation
     *
     * @see BistouryDebugClient
     * @see BistouryMonitorClient
     */
    @Synchronized
    @JvmStatic
    fun init(instrumentation: Instrumentation) {
        // 如果已经完成初始化, 那么return
        if (!inited.compareAndSet(false, true)) {
            return
        }
        System.setProperty("bistoury.app.lib.class", "com.wanna.debugger.bistoury.instrument.client.common.InstrumentClientStore")
        // 应用程序的Jar包当中的类的计算的Supplier(从"bistoury.app.lib.class"系统属性当中去进行读取)
        val appLibClassSupplier = DefaultAppLibClassSupplier(instrumentation)

        // 应用程序的ClassPath的计算的Supplier
        val appClassPathSupplier = DefaultAppClassPathSupplier(appLibClassSupplier)

        // 封装成为InstrumentInfo
        val instrumentInfo = InstrumentInfo(
            instrumentation,
            this.lock,
            DefaultClassFileBuffer,
            appLibClassSupplier,
            appClassPathSupplier
        )

        val instrumentClients = ArrayList<InstrumentClient>()


        // 创建DebugClient
        instrumentClients += BistouryDebugClients.create(instrumentInfo)

        // 创建MonitorClient
        instrumentClients += BistouryMonitorClients.create(instrumentInfo)

        InstrumentClientStore.instrumentInfo = instrumentInfo
        InstrumentClientStore.instrumentClients = Collections.unmodifiableList(instrumentClients)
    }

}