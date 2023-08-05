package com.wanna.debugger.bistoury.instrument.client.debugger.bean

/**
 * 封装执行某一行代码时的上下文快照信息, 获取到快照只是去用于去进行条件断点的匹配,
 * 对于编写的断点的表达式当中的"localVariables"/"fields"/"staticFields", 就是遵循的这个规范.
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 *
 * @param localVariables 局部变量信息
 * @param fields 字段信息
 * @param staticFields static字段信息
 */
data class ConditionBreakpointSnapshot @JvmOverloads constructor(
    var localVariables: Map<String, Any?> = emptyMap(),
    var fields: Map<String, Any?> = emptyMap(),
    var staticFields: Map<String, Any?> = emptyMap()
)