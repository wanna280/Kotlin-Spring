package com.wanna.debugger.bistoury.instrument.client.monitor

import com.wanna.debugger.bistoury.instrument.client.common.InstrumentClient
import com.wanna.debugger.bistoury.instrument.client.common.InstrumentInfo

/**
 * Bistoury MonitorClient
 *
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/31
 *
 * @param instrumentInfo InstrumentInfo
 */
open class BistouryMonitorClient(instrumentInfo: InstrumentInfo) : InstrumentClient {

    /**
     * BistouryMonitor, 去提供动态添加/移除监控的功能实现
     */
    private val bistouryMonitor: BistouryMonitor = DefaultBistouryMonitor()

    init {
        // 启动BistouryMonitor
        bistouryMonitor.startup(instrumentInfo)
    }


    /**
     * 对外提供方法, 给代码当中的某一行去添加一个动态监控
     *
     * @param sourceJavaFile 要去添加监控的类的Java文件路径(例如"com/wanna/Test.java")
     * @param lineNumber 要去添加监控的行号
     * @return 添加成功的监控的monitorId
     */
    open fun addMonitor(sourceJavaFile: String, lineNumber: Int): String {
        return bistouryMonitor.addMonitor(sourceJavaFile, lineNumber)
    }

    /**
     * 对外提供方法, 将代码当中的某一行的已经存在的监控去进行移除
     *
     * @param className 要去移除监控的类
     * @param lineNumber 要去移除监控的行号
     * @param monitorId 要去移除监控的monitorId
     */
    open fun removeMonitor(className: String, lineNumber: Int, monitorId: String) {
        bistouryMonitor.removeMonitor(className, lineNumber, monitorId)
    }

    /**
     * 在Bistoury MonitorClient关闭时, 同时去关闭BistouryMonitor
     */
    override fun destroy() {
        this.bistouryMonitor.destroy()
    }

}