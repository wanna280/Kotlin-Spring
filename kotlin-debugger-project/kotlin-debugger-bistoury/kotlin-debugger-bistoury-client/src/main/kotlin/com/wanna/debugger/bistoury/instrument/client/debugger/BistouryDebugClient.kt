package com.wanna.debugger.bistoury.instrument.client.debugger

import com.wanna.debugger.bistoury.instrument.client.common.InstrumentClient
import com.wanna.debugger.bistoury.instrument.client.common.InstrumentInfo
import com.wanna.debugger.bistoury.instrument.client.common.NamedThreadFactory
import com.wanna.debugger.bistoury.instrument.client.debugger.bean.BreakpointSnapshot
import java.util.concurrent.Executors
import javax.annotation.Nullable

/**
 * Bistoury DebugClient, 对外提供运行时的动态Debug功能的实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 *
 * @param instrumentInfo InstrumentInfo
 */
open class BistouryDebugClient(instrumentInfo: InstrumentInfo) : InstrumentClient {

    /**
     * BistouryDebugger对象, 用于提供运行时去动态注册断点/移除断点等相关功能
     */
    private val bistouryDebugger: BistouryDebugger = BistouryDefaultDebugger()

    /**
     * BreakpointSnapshot Cache, 负责去保存断点的快照缓存信息, 可以根据断点ID去从Cache当中去获取断点的快照信息
     */
    private val breakpointSnapshotCache: BreakpointSnapshotCache

    /**
     * 断点的缓存的清理的线程池(每隔1分钟去检查一下, 断点的缓存信息是否过期? 对于断点缓存的过期时间为10分钟)
     */
    private val breakpointCacheCleanExecutor =
        Executors.newSingleThreadScheduledExecutor(NamedThreadFactory("breakpoint-clean-worker"))

    init {
        // 创建一个监听断点快照信息的缓存移除消息的Listener
        // 当快照信息被移除时, 会自动回调这个Listener, 从Debugger当中去移除这个断点...
        val removalListener = BreakpointSnapshotRemovalListener { breakpointId, snapshot ->
            this.bistouryDebugger.deregisterBreakpoint(snapshot.sourceJavaFile, snapshot.lineNumber, breakpointId)
        }

        // 创建一个断点快照信息的缓存对象, 提供对于断点的快照信息的缓存
        val snapshotCache =
            DefaultBreakpointBreakpointSnapshotStore(instrumentInfo.lock, removalListener, breakpointCacheCleanExecutor)

        // 启动Debugger
        this.bistouryDebugger.startup(instrumentInfo, snapshotCache)

        // 保存Snapshot和Debugger
        this.breakpointSnapshotCache = snapshotCache
    }

    /**
     * 对外提供方法, 为给定的类的源码的指定行上去打个断点
     *
     * @param sourceJavaFile 要去进行打断点的类的Java文件路径(例如"com/wanna/Test.java")
     * @param lineNumber 要打断点的行号
     * @param breakpointCondition 断点条件
     * @return 注册成功的断点的断点ID, 后续可以根据这个断点ID去进行删除断点/获取断点的快照信息数据
     */
    open fun registerBreakpoint(sourceJavaFile: String, lineNumber: Int, breakpointCondition: String): String {
        return this.bistouryDebugger.registerBreakpoint(sourceJavaFile, lineNumber, breakpointCondition)
    }

    /**
     * 对外提供方法, 库根据断点ID去获取该断点位置的快照信息
     *
     * @param breakpointId 断点ID
     * @return 该断点位置的快照信息(如果该断点不存在快照信息的话, 那么return null)
     */
    @Nullable
    open fun getBreakpointSnapshot(breakpointId: String): BreakpointSnapshot? {
        return this.breakpointSnapshotCache.getBreakpointSnapshot(breakpointId)
    }

    /**
     * 对外提供方法, 根据断点ID去移除断点信息
     *
     * @param breakpointId 断点ID
     */
    open fun removeBreakpoint(breakpointId: String) {
        val snapshot = breakpointSnapshotCache.getBreakpointSnapshot(breakpointId) ?: return

        // 从Debugger当中去移除断点
        bistouryDebugger.deregisterBreakpoint(snapshot.sourceJavaFile, snapshot.lineNumber, snapshot.breakpointId)

        // 从Cache当中去移除该断点的快照信息
        breakpointSnapshotCache.removeBreakpointSnapshot(breakpointId)
    }

    /**
     * 当DebugClient关闭时, 需要同时去关闭Debugger
     */
    override fun destroy() {
        this.bistouryDebugger.destroy()
    }
}