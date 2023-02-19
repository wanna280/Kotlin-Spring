package com.wanna.debugger.bistoury.instrument.spy

import java.lang.reflect.Method
import javax.annotation.Nullable

/**
 * 保存一下全局的方法, 方便ASM框架去进行字节码的生成时, 可以快速访问到这个类当中的方法.
 *
 * Note: 对于这个类当中的方法, 都会使用ASM生成字节码的方式去进行调用, 不会被直接进行调用,
 * 因此Intellij IDEA等IDE, 根本搜索不到这个类当中的相关方法的引用
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 */
object BistourySpy {

    const val HAS_BREAK_POINT_SET_METHOD_NAME = "hasBreakpointSet"

    const val IS_HIT_BREAKPOINT_METHOD_NAME = "isHitBreakpoint"

    const val PUT_LOCAL_VARIABLE_METHOD_NAME = "putLocalVariable"

    const val PUT_FIELD_METHOD_NAME = "putField"

    const val PUT_STATIC_METHOD_NAME = "putStaticField"

    const val FILL_STACK_TRACE_METHOD_NAME = "fillStacktrace"

    const val DUMP_METHOD_NAME = "dump"

    const val END_RECEIVE_METHOD_NAME = "endReceive"

    const val START_MONITOR_METHOD_NAME = "start"

    const val STOP_MONITOR_METHOD_NAME = "stop"

    const val EXCEPTION_MONITOR_METHOD_NAME = "exception"


    /**
     * 检查给定的className&lineNumber处, 是否存在有断点的方法
     *
     * @see com.wanna.debugger.bistoury.instrument.client.debugger.BistouryGlobalDebugContext.hasBreakpointSet
     */
    @Volatile
    @JvmStatic
    private var HAS_BREAKPOINT_SET_METHOD: Method? = null

    /**
     * 检查是否命中断点的方法
     *
     * @see com.wanna.debugger.bistoury.instrument.client.debugger.BistouryGlobalDebugContext.isHitBreakpoint
     */
    @Volatile
    @JvmStatic
    private var IS_HIT_BREAKPOINT_METHOD: Method? = null

    /**
     * 添加局部变量的方法
     *
     * @see com.wanna.debugger.bistoury.instrument.client.debugger.BreakpointSnapshotCapture.putLocalVariable
     */
    @Volatile
    @JvmStatic
    private var PUT_LOCAL_VARIABLE_METHOD: Method? = null

    /**
     * 添加成员变量的方法
     *
     * @see com.wanna.debugger.bistoury.instrument.client.debugger.BreakpointSnapshotCapture.putField
     */
    @Volatile
    @JvmStatic
    private var PUT_FIELD_METHOD: Method? = null

    /**
     * 添加static类变量的方法
     *
     * @see com.wanna.debugger.bistoury.instrument.client.debugger.BreakpointSnapshotCapture.putStaticField
     */
    @Volatile
    @JvmStatic
    private var PUT_STATIC_FIELD_METHOD: Method? = null

    /**
     * 填充断点的栈轨迹的方法
     *
     * @see com.wanna.debugger.bistoury.instrument.client.debugger.BreakpointSnapshotCapture.fillStacktrace
     */
    @Volatile
    @JvmStatic
    private var FILL_STACK_TRACE_METHOD: Method? = null

    /**
     * 将快照信息保存到[com.wanna.debugger.bistoury.instrument.client.debugger.BreakpointSnapshotReceiver]当中的方法
     *
     * @see com.wanna.debugger.bistoury.instrument.client.debugger.BreakpointSnapshotCapture.dump
     */
    @Volatile
    @JvmStatic
    private var DUMP_METHOD: Method? = null

    /**
     * 结束断点的方法
     *
     * @see com.wanna.debugger.bistoury.instrument.client.debugger.BreakpointSnapshotCapture.endReceive
     */
    @Volatile
    @JvmStatic
    private var END_RECEIVE_METHOD: Method? = null

    /**
     * 开始进行监控指标的记录的方法
     *
     * @see com.wanna.debugger.bistoury.instrument.client.monitor.BistouryAgentMonitor.start
     */
    @Volatile
    @JvmStatic
    private var START_MONITOR_METHOD: Method? = null

    /**
     * 结束监控指标的记录的方法
     *
     * @see com.wanna.debugger.bistoury.instrument.client.monitor.BistouryAgentMonitor.stop
     */
    @Volatile
    @JvmStatic
    private var STOP_MONITOR_METHOD: Method? = null

    /**
     * 记录异常监控的方法
     *
     * @see com.wanna.debugger.bistoury.instrument.client.monitor.BistouryAgentMonitor.exception
     */
    @Volatile
    @JvmStatic
    private var EXCEPTION_MONITOR_METHOD: Method? = null


    /**
     * 初始化当前[BistourySpy], 将ASM生成字节码时需要用到的相关方法, 都去保存到这个类当中来
     */
    @JvmStatic
    fun init(
        hasBreakpointSetMethod: Method,
        isHitBreakpointMethod: Method,
        putLocalVariableMethod: Method,
        putFieldMethod: Method,
        putStaticFieldMethod: Method,
        fillStackTraceMethod: Method,
        dumpMethod: Method,
        endReceiveMethod: Method,
        startMonitorMethod: Method,
        stopMonitorMethod: Method,
        exceptionMonitorMethod: Method
    ) {
        this.HAS_BREAKPOINT_SET_METHOD = hasBreakpointSetMethod
        this.IS_HIT_BREAKPOINT_METHOD = isHitBreakpointMethod
        this.PUT_LOCAL_VARIABLE_METHOD = putLocalVariableMethod
        this.PUT_FIELD_METHOD = putFieldMethod
        this.PUT_STATIC_FIELD_METHOD = putStaticFieldMethod
        this.FILL_STACK_TRACE_METHOD = fillStackTraceMethod
        this.DUMP_METHOD = dumpMethod
        this.END_RECEIVE_METHOD = endReceiveMethod
        this.START_MONITOR_METHOD = startMonitorMethod
        this.STOP_MONITOR_METHOD = stopMonitorMethod
        this.EXCEPTION_MONITOR_METHOD = exceptionMonitorMethod
    }


    @JvmStatic
    fun hasBreakpointSet(source: String, lineNumber: Int): Boolean {
        return doInvokeMethod(HAS_BREAKPOINT_SET_METHOD, arrayOf(source, lineNumber), false) as Boolean
    }

    @JvmStatic
    fun isHitBreakpoint(source: String, lineNumber: Int): Boolean {
        return doInvokeMethod(IS_HIT_BREAKPOINT_METHOD, arrayOf(source, lineNumber), false) as Boolean
    }

    @JvmStatic
    fun putLocalVariable(name: String, @Nullable value: Any?) {
        doInvokeMethod(PUT_LOCAL_VARIABLE_METHOD, arrayOf(name, value), null)
    }

    @JvmStatic
    fun putStaticField(name: String, @Nullable value: Any?) {
        doInvokeMethod(PUT_STATIC_FIELD_METHOD, arrayOf(name, value), null)
    }

    @JvmStatic
    fun putField(name: String, @Nullable value: Any?) {
        doInvokeMethod(PUT_FIELD_METHOD, arrayOf(name, value), null)
    }

    @JvmStatic
    fun fillStacktrace(source: String, lineNumber: Int, ex: Throwable) {
        doInvokeMethod(FILL_STACK_TRACE_METHOD, arrayOf(source, lineNumber, ex), null)
    }

    @JvmStatic
    fun dump(source: String, lineNumber: Int) {
        doInvokeMethod(DUMP_METHOD, arrayOf(source, lineNumber), null)
    }

    @JvmStatic
    fun endReceive(source: String, lineNumber: Int) {
        doInvokeMethod(END_RECEIVE_METHOD, arrayOf(source, lineNumber), null)
    }

    @JvmStatic
    fun start(): Long {
        return doInvokeMethod(START_MONITOR_METHOD, emptyArray(), null) as Long
    }

    @JvmStatic
    fun stop(key: String, startTime: Long) {
        doInvokeMethod(STOP_MONITOR_METHOD, arrayOf(key, startTime), null)
    }

    @JvmStatic
    fun exception(key: String) {
        doInvokeMethod(EXCEPTION_MONITOR_METHOD, arrayOf(key), null)
    }

    @Nullable
    private fun doInvokeMethod(method: Method?, args: Array<Any?>, @Nullable defaultValue: Any?): Any? {
        return method?.invoke(null, *args) ?: defaultValue
    }


}