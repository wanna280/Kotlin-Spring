package com.wanna.debugger.bistoury.instrument.client.common

import java.lang.instrument.Instrumentation
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 完成Bistoury的引导和启动的启动类
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 */
object BistouryBootstrap {

    /**
     * 进程PID
     */
    @JvmStatic
    private var pid: Int = -1

    /**
     * Instrumentation
     */
    @JvmStatic
    private var instrumentation: Instrumentation? = null

    /**
     * 是否已经完成绑定的标志位
     */
    private var isBound = AtomicBoolean()

    /**
     * 完成[BistouryBootstrap]对象的初始化,
     *
     * Note: 这个方法会被Agent反射回调
     *
     * @param pid pid
     * @param instrumentation Instrumentation
     */
    @JvmStatic
    fun init(pid: Int, instrumentation: Instrumentation) {
        BistouryBootstrap.pid = pid
        BistouryBootstrap.instrumentation = instrumentation
    }

    /**
     * 检查是否已经完成过绑定了?
     *
     * Note: 这个方法会被Agent反射回调
     *
     * @return 如果之前已经进行过绑定, 那么return true; 没有进行过绑定则return false
     */
    @JvmStatic
    fun isBound(): Boolean {
        return isBound.get()
    }

    /**
     * 完成BistouryBootstrap的绑定工作
     *
     * Note: 这个方法会被Agent反射回调
     */
    @JvmStatic
    fun bind() {
        // CAS, 避免出现并发绑定的情况...
        if (!isBound.compareAndSet(false, true)) {
            return
        }

        // 将Instrumentation去保存到InstrumentClientStore当中去...
        // 并完成各个InstrumentClient(例如DebugClient)的初始化
        InstrumentClientStore.init(instrumentation!!)

        // TODO create ShellServer to process Commands
    }
}