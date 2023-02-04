package com.wanna.debugger.bistoury.instrument.client.monitor

import com.wanna.debugger.bistoury.instrument.client.common.InstrumentInfo

/**
 * BistouryMonitor, 提供运行时的动态监控的相关功能的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/31
 */
interface BistouryMonitor {

    /**
     * 启动提供动态监控的[BistouryMonitor]
     *
     * @param instrumentInfo InstrumentInfo
     */
    fun startup(instrumentInfo: InstrumentInfo): Boolean

    /**
     * 给类当中的某一行代码去添加动态监控
     *
     * @param sourceJavaFile 要去添加监控的类的Java文件路径(例如"com/wanna/Test.java")
     * @param lineNumber 要去添加监控的代码行号
     * @return 添加成功的监控的monitorId
     */
    fun addMonitor(sourceJavaFile: String, lineNumber: Int): String

    /**
     * 给类当中的某一行代码的已经添加的监控去进行移除
     *
     * @param sourceJavaFile 要去移除监控的类的Java文件路径(例如"com/wanna/Test.java")
     * @param lineNumber 要去移除监控的行号
     * @param monitorId 监控ID
     */
    fun removeMonitor(sourceJavaFile: String, lineNumber: Int, monitorId: String)

    /**
     * 摧毁当前的[BistouryMonitor]
     */
    fun destroy()
}