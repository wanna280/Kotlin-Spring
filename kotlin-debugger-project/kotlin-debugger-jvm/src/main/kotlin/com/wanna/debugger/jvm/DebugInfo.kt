package com.wanna.debugger.jvm

import javax.annotation.Nullable

/**
 * 封装Debug请求的响应信息
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 */
open class DebugInfo {
    /**
     * tag
     */
    var tag: String? = null

    /**
     * 当前方法所在的栈帧相关信息
     */
    var current: StackTraceElement? = null

    /**
     * 本次Debug是否已经结束?
     */
    var isEnd = false

    /**
     * StackTrace信息
     */
    var stackTraces: MutableList<StackTraceElement> = ArrayList()

    /**
     * 字段变量信息
     */
    var fieldObjects: MutableList<ObjectInfo> = ArrayList()

    /**
     * static变量信息
     */
    var staticObjects: MutableList<ObjectInfo> = ArrayList()

    /**
     * 局部变量信息
     */
    var localVariables: MutableList<ObjectInfo> = ArrayList()



    data class StackTraceElement(val className: String, val methodName: String, val lineNumber: Int)

    data class ObjectInfo(val name: String, val type: String, @Nullable val value: Any?)

    override fun toString(): String {
        return "DebugInfo(tag=$tag, current=$current, isEnd=$isEnd, stackTraces=$stackTraces, fieldObjects=$fieldObjects, staticObjects=$staticObjects, localVariables=$localVariables)"
    }
}