package com.wanna.debugger.bistoury.instrument.client.debugger

/**
 * 断点的快照信息的Receiver回调函数, 用于回调相关的方法, 去对断点的相关信息去进行保存
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 */
interface BreakpointSnapshotReceiver {

    /**
     * 刷新断点的过期时间
     *
     * @param breakpointId 断点ID
     */
    fun refreshBreakpointExpireTime(breakpointId: String)

    /**
     * 初始化给定位置的断点的快照信息
     *
     * @param breakpointId 断点ID
     * @param sourceJavaFile 断点所在的类的Java文件路径(例如"com/wanna/Test.java")
     * @param lineNumber 断点的行号
     */
    fun initBreakPoint(breakpointId: String, sourceJavaFile: String, lineNumber: Int)

    /**
     * 将给定的断点的局部变量信息去保存下来
     *
     * @param breakpointId 断点ID
     * @param localVariables 局部变量表当中的变量信息
     */
    fun putLocalVariables(breakpointId: String, localVariables: Map<String, Any?>)

    /**
     * 将给定的断点的字段信息去进行保存下来
     *
     * @param breakpointId 断点ID
     * @param fields 字段信息
     */
    fun putFields(breakpointId: String, fields: Map<String, Any?>)

    /**
     * 将给定的断点的static字段信息去保存下来
     *
     * @param breakpointId 断点ID
     * @param staticFields 静态字段信息
     */
    fun putStaticFields(breakpointId: String, staticFields: Map<String, Any?>)

    /**
     * 将给定的断点的快照信息去进行保存下来
     *
     * @param breakpointId 断点ID
     * @param stackTrace 栈轨迹
     */
    fun fillStacktrace(breakpointId: String, stackTrace: Array<StackTraceElement>)

    /**
     * 设置断点的源码所在类
     *
     * @param breakpointId 断点ID
     * @param sourceJavaFile 类的Java文件路径(例如"com/wanna/Test.java")
     */
    fun setSourceClass(breakpointId: String, sourceJavaFile: String)

    /**
     * 设置给定的断点所在的行号
     *
     * @param breakpointId 断点ID
     * @param lineNumber 断点行号
     */
    fun setLineNumber(breakpointId: String, lineNumber: Int)

    /**
     * 断点结束, 停止接收数据
     *
     * @param breakpointId 断点ID
     */
    fun endReceive(breakpointId: String)

    /**
     * 结束断点失败
     *
     * @param breakpointId 断点ID
     */
    fun endFail(breakpointId: String)

    /**
     * 根据给定的断点ID, 去移除该断点的快照信息
     *
     * @param breakpointId 断点ID
     */
    fun removeBreakpointSnapshot(breakpointId: String)
}