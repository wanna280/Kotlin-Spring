package com.wanna.debugger.bistoury.instrument.client.debugger.bean

/**
 * 添加断点的结果
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 *
 * @param id 添加的断点的断点ID
 * @param newId 是否是一个新的断点ID?
 */
data class AddBreakpointResult(val id: String, val newId: Boolean)