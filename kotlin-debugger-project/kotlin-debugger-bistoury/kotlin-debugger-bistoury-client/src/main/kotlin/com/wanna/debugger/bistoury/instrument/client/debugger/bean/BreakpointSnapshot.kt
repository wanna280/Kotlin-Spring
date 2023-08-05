package com.wanna.debugger.bistoury.instrument.client.debugger.bean

/**
 * 维护一个断点停下时的快照信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 *
 * @param breakpointId 断点的ID
 * @param sourceJavaFile 断点所在的类的Java文件路径(例如"com/wanna/Test.java")
 * @param lineNumber 断点所在的代码行号
 * @param expireTime 该断点的过期时间时间戳
 */
data class BreakpointSnapshot(val breakpointId: String, val sourceJavaFile: String, val lineNumber: Int, var expireTime: Long) {

    /**
     * 局部变量表的相关信息
     */
    var localVariables: Map<String, Any?> = emptyMap()

    /**
     * 字段的相关信息
     */
    var fields: Map<String, Any?> = emptyMap()

    /**
     * static静态变量的相关信息
     */
    var staticFields: Map<String, Any?> = emptyMap()

    /**
     * stackTrace的相关信息
     */
    var stackTrace: Any? = null
}