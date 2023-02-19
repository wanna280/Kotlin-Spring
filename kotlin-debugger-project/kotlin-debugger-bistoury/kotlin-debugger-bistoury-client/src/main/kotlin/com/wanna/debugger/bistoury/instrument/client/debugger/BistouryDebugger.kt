package com.wanna.debugger.bistoury.instrument.client.debugger

import com.wanna.debugger.bistoury.instrument.client.common.InstrumentInfo

/**
 * 提供对于一个Debugger的各个操作的接口
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 *
 * @see BistouryDefaultDebugger
 */
interface BistouryDebugger {

    /**
     * 根据给定的[InstrumentInfo]和[BreakpointSnapshotReceiver]相关信息, 去启动[BistouryDebugger]
     *
     * @param instrumentInfo InstrumentInfo
     * @param receiver BreakpointSnapshotReceiver
     */
    fun startup(instrumentInfo: InstrumentInfo, receiver: BreakpointSnapshotReceiver)

    /**
     * 给指定的类的某个代码行, 去添加一个断点
     *
     * @param sourceJavaFile 要去进行打断点的类的Java文件路径(例如"com/wanna/Test.java")
     * @param lineNumber 要去进行打断点的源码行号
     * @param breakpointCondition 断点条件
     * @return 注册的断点的ID
     */
    fun registerBreakpoint(sourceJavaFile: String, lineNumber: Int, breakpointCondition: String): String

    /**
     * 给指定的类的某个代码行上的断点, 去进行取消注册(移除)一个断点
     *
     * @param sourceJavaFile 要取消注册断点的类(例如"com/wanna/Test.java")
     * @param lineNumber 要去取消注册断点的类的源码行
     * @param breakpointId 要去取消断点的断点ID
     */
    fun deregisterBreakpoint(sourceJavaFile: String, lineNumber: Int, breakpointId: String)

    /**
     * 摧毁当前[BistouryDebugger]
     */
    fun destroy()
}