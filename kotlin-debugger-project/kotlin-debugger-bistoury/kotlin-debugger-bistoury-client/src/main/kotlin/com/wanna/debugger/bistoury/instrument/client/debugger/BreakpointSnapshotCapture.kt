package com.wanna.debugger.bistoury.instrument.client.debugger

import com.wanna.debugger.bistoury.instrument.client.debugger.bean.ConditionBreakpointSnapshot
import javax.annotation.Nullable

/**
 * 提供对于Debug的快照的抓取, 基于[ThreadLocal]去进行保存执行断点时的局部变量/成员变量/静态变量
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 */
object BreakpointSnapshotCapture {

    /**
     * 快照的局部变量表信息
     */
    @JvmStatic
    private val localVariables = object : ThreadLocal<MutableMap<String, Any?>>() {
        override fun initialValue(): MutableMap<String, Any?> = LinkedHashMap()
    }

    /**
     * 快照的字段信息
     */
    @JvmStatic
    private val fields = object : ThreadLocal<MutableMap<String, Any?>>() {
        override fun initialValue(): MutableMap<String, Any?> = LinkedHashMap()
    }

    /**
     * 快照的static字段信息
     */
    @JvmStatic
    private val staticFields = object : ThreadLocal<MutableMap<String, Any?>>() {
        override fun initialValue(): MutableMap<String, Any?> = LinkedHashMap()
    }

    /**
     * 获取当前时刻正在处理的断点的断点快照信息
     *
     * @return 当前时刻正在进行处理的断点的快照信息
     */
    @JvmStatic
    fun get(): ConditionBreakpointSnapshot {
        return ConditionBreakpointSnapshot(localVariables.get(), fields.get(), staticFields.get())
    }

    /**
     * 添加localVariable局部变量(会被反射调用)
     *
     * @param name 局部变量名
     * @param value 局部变量值
     */
    @JvmStatic
    fun putLocalVariable(name: String, @Nullable value: Any?) {
        value ?: return
        localVariables.get()[name] = value
    }

    /**
     * 添加field成员变量字段
     *
     * @param name 成员变量名
     * @param value 成员变量值
     */
    @JvmStatic
    fun putField(name: String, @Nullable value: Any?) {
        value ?: return
        fields.get()[name] = value
    }

    /**
     * 添加static类变量字段
     *
     * @param name static类变量名
     * @param value static类变量值
     */
    @JvmStatic
    fun putStaticField(name: String, @Nullable value: Any?) {
        value ?: return
        staticFields.get()[name] = value
    }

    /**
     * 填充断点的stacktrace栈轨迹
     *
     * @param sourceJavaFile 类的Java文件路径(例如"com/wanna/Test.java")
     * @param lineNumber 行号
     * @param ex 用于填充栈轨迹的异常
     */
    @JvmStatic
    fun fillStacktrace(sourceJavaFile: String, lineNumber: Int, ex: Throwable) {
        val breakpointId = BistouryGlobalDebugContext.getBreakpointId()
        if (breakpointId.isNullOrBlank()) {
            return
        }
        val snapshotReceiver = BistouryGlobalDebugContext.getSnapshotReceiver()
        snapshotReceiver.fillStacktrace(breakpointId, ex.stackTrace)
    }

    /**
     * 将给定的类的指定行号的快照信息(局部变量/字段/static字段)去进行dump到缓存当中
     *
     * @param sourceJavaFile 类的Java文件路径(例如"com/wanna/Test.java")
     * @param lineNumber 类的源代码行号
     */
    @JvmStatic
    fun dump(sourceJavaFile: String, lineNumber: Int) {
        val breakpointId = BistouryGlobalDebugContext.getBreakpointId()
        if (breakpointId.isNullOrBlank()) {
            return
        }

        val snapshotReceiver = BistouryGlobalDebugContext.getSnapshotReceiver()

        // 保存局部变量信息
        val localVariables = localVariables.get()
        if (localVariables != null && localVariables.isNotEmpty()) {
            snapshotReceiver.putLocalVariables(breakpointId, localVariables)
        }

        // 保存fields信息
        val fields = fields.get()
        if (fields != null && fields.isNotEmpty()) {
            snapshotReceiver.putFields(breakpointId, fields)
        }

        // 保存static变量信息...
        val staticFields = staticFields.get()
        if (staticFields != null && staticFields.isNotEmpty()) {
            snapshotReceiver.putStaticFields(breakpointId, staticFields)
        }
    }

    /**
     * 结束断点, 清除当前ThreadLocal当中的快照信息
     *
     * @param sourceJavaFile 类的Java文件路径(例如"com/wanna/Test.java")
     * @param lineNumber 类的源代码行号
     */
    @JvmStatic
    fun endReceive(sourceJavaFile: String, lineNumber: Int) {
        val breakpointId = BistouryGlobalDebugContext.getBreakpointId()
        if (breakpointId.isNullOrBlank()) {
            return
        }
        val snapshotReceiver = BistouryGlobalDebugContext.getSnapshotReceiver()
        try {
            snapshotReceiver.setSourceClass(breakpointId, sourceJavaFile)
            snapshotReceiver.setLineNumber(breakpointId, lineNumber)
            snapshotReceiver.endReceive(breakpointId)
        } finally {

            // reset ThreadLocalCache
            reset()
        }
    }

    /**
     * 重设快照信息, 需要清除缓存信息
     */
    @JvmStatic
    private fun reset() {
        localVariables.remove()
        fields.remove()
        staticFields.remove()
    }
}