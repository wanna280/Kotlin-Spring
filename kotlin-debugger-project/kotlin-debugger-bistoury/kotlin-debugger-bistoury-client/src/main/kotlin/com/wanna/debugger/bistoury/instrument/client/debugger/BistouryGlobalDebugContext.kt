package com.wanna.debugger.bistoury.instrument.client.debugger

import com.wanna.debugger.bistoury.instrument.client.debugger.bean.AddBreakpointResult
import com.wanna.debugger.bistoury.instrument.client.debugger.bean.Breakpoint
import com.wanna.debugger.bistoury.instrument.client.location.Location
import java.util.*
import javax.annotation.Nullable

/**
 * 全局的DebugContext
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 */
object BistouryGlobalDebugContext {

    /**
     * 对于正常的断点(非条件断点)的后缀标识
     */
    private const val NORMAL_BREAKPOINT_SUFFIX = "-n"

    /**
     * 对于条件断点的后缀标识
     */
    private const val CONDITION_BREAKPOINT_SUFFIX = "-c"

    /**
     * 全局的断点列表, 添加断点时需要加锁保证并发安全
     */
    @JvmStatic
    private val breakpoints = LinkedHashMap<Location, Breakpoint>()

    /**
     * 使用ThreadLocal去进行记录当前正在处理的断点ID(breakpointId)
     */
    @JvmStatic
    private val currentBreakpointId = ThreadLocal<String>()

    /**
     * BreakpointSnapshot Receiver
     */
    @Nullable
    private var breakpointSnapshotReceiver: BreakpointSnapshotReceiver? = null

    /**
     * 初始化SnapshotReceiver
     *
     * @param breakpointSnapshotReceiver BreakpointSnapshotReceiver
     */
    @JvmStatic
    fun initSnapshotReceiver(breakpointSnapshotReceiver: BreakpointSnapshotReceiver) {
        this.breakpointSnapshotReceiver = breakpointSnapshotReceiver
    }

    /**
     * 获取对断点的快照信息去进行操作的SnapshotReceiver(断点的快照缓存)
     *
     * @return BreakpointSnapshotReceiver
     */
    @JvmStatic
    fun getSnapshotReceiver(): BreakpointSnapshotReceiver =
        this.breakpointSnapshotReceiver ?: throw IllegalStateException("BreakpointSnapshotReceiver cannot be null")

    /**
     * 给指定的代码位置去添加断点
     *
     * @param codeLocation 要添加断点的位置
     * @param condition  断点的条件(为空串代表非条件断点)
     */
    @JvmStatic
    fun addBreakpoint(codeLocation: Location, condition: String): AddBreakpointResult {
        synchronized(breakpoints) {
            // 使用UUID生成断点ID, 如果是正常断点后缀添加"-n", 如果是条件断点, 后缀添加"-c"
            val breakpointId = UUID.randomUUID().toString() +
                    if (condition.isNotBlank()) CONDITION_BREAKPOINT_SUFFIX else NORMAL_BREAKPOINT_SUFFIX

            breakpoints[codeLocation] = Breakpoint(breakpointId, codeLocation, condition)
            return AddBreakpointResult(breakpointId, true)
        }
    }

    /**
     * 根据代码位置和断点ID去移除一个已经添加的断点
     *
     * @param codeLocation 断点的代码位置
     * @param breakpointId 断点ID
     */
    @JvmStatic
    fun removeBreakpoint(codeLocation: Location, breakpointId: String) {
        synchronized(breakpoints) {
            val breakpoint = breakpoints[codeLocation]

            // 如果已经存在的断点和断点ID匹配的话, 移除掉
            if (breakpoint != null && breakpoint.id == breakpointId) {
                breakpoints.remove(codeLocation)
            }
        }
    }

    /**
     * 移除给定的断点
     *
     * @param breakpoint 要去进行移除的断点
     */
    @JvmStatic
    fun removeBreakpoint(breakpoint: Breakpoint) {
        removeBreakpoint(breakpoint.codeLocation, breakpoint.id)
    }

    /**
     * 根据代码位置和断点ID去移除一个已经添加的断点
     *
     * @param className 断点的源码类名
     * @param lineNumber 断点的类的行号
     * @param breakpointId 断点ID
     */
    @JvmStatic
    fun removeBreakpoint(className: String, lineNumber: Int, breakpointId: String) {
        removeBreakpoint(Location(className, lineNumber), breakpointId)
    }

    /**
     * 检查给定的代码位置, 是否存在有断点?
     *
     * @param codeLocation 要去进行检查的代码位置
     * @return 如果已经设置了断点, return true; 如果没有设置的话, return false
     */
    @JvmStatic
    fun hasBreakpointSet(codeLocation: Location): Boolean {
        synchronized(breakpoints) {
            return breakpoints.containsKey(codeLocation)
        }
    }

    /**
     * 检查给定的代码位置, 是否存在有断点?
     *
     * @param sourceJavaFile 要去检查断点的类的Java文件路径(例如"com/wanna/Test.java")
     * @param lineNumber 要去进行检查断点的类对应的代码行
     * @return 如果已经设置了断点, return true; 如果没有设置的话, return false
     */
    @JvmStatic
    fun hasBreakpointSet(sourceJavaFile: String, lineNumber: Int): Boolean {
        return hasBreakpointSet(Location(sourceJavaFile, lineNumber))
    }

    /**
     * 获取当前正在处理的断点ID
     *
     * @return 正在处理的断点的断点ID(不存在的话, return null)
     */
    @Nullable
    @JvmStatic
    fun getBreakpointId(): String? {
        return currentBreakpointId.get()
    }

    /**
     * 触发断点, 将当前正在处理的断点ID去保存到ThreadLocal当中去...
     *
     * @param breakpoint 要去进行执行的断点
     */
    @JvmStatic
    fun doBreak(breakpoint: Breakpoint): Boolean {
        if (breakpoint.trigger()) {
            removeBreakpoint(breakpoint)
            currentBreakpointId.set(breakpoint.id)
            return true
        }
        return false
    }

    /**
     * 检查给定的代码行是否命中断点
     *
     * @param sourceJavaFile 要去进行检查的类的Java文件路径(例如"com/wanna/Test.java")
     * @param lineNumber 要去进行的指定的类的代码行
     */
    @JvmStatic
    fun isHitBreakpoint(sourceJavaFile: String, lineNumber: Int): Boolean {
        if (breakpointSnapshotReceiver === null) return false

        // 如果给定的位置确实是不存在断点的话, 那么skip跳过
        val breakpoint: Breakpoint
        synchronized(breakpoints) {
            breakpoint = breakpoints[Location(sourceJavaFile, lineNumber)] ?: return false
        }

        // 如果给定的位置存在断点的话, 那么...需要去进行处理

        // 如果该位置的断点的条件, 为空的话, 那么直接去触发断点...
        if (breakpoint.condition.isBlank()) {
            return doBreak(breakpoint)
        }

        // 如果该位置的断点的条件不为空, 那么需要检查快照信息和条件去进行匹配...
        val breakpointSnapshot = BreakpointSnapshotCapture.get()

        // TODO 条件匹配...
        return doBreak(breakpoint)
    }

    /**
     * 关闭当前DebugContext, 需要清除所有的断点
     */
    @JvmStatic
    fun destroy() {
        synchronized(breakpoints) {
            breakpoints.clear()
            currentBreakpointId.remove()
        }
    }
}