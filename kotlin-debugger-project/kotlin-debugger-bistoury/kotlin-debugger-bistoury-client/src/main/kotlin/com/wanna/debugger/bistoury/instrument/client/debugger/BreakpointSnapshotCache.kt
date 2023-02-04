package com.wanna.debugger.bistoury.instrument.client.debugger

import com.wanna.debugger.bistoury.instrument.client.debugger.bean.BreakpointSnapshot
import javax.annotation.Nullable

/**
 * 断点的快照信息的缓存
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 */
interface BreakpointSnapshotCache {

    /**
     * 根据断点ID去移除掉一个断点的快照信息
     *
     * @param breakpointId 断点ID
     * @return 根据断点ID去获取到的断点的快照信息(如果不存在这样的快照信息, return null)
     */
    @Nullable
    fun getBreakpointSnapshot(breakpointId: String): BreakpointSnapshot?

    /**
     * 根据断点ID去移除一个断点的快照信息
     *
     * @param breakpointId 断点ID
     */
    fun removeBreakpointSnapshot(breakpointId: String)
}