package com.wanna.debugger.bistoury.instrument.client.debugger

import com.wanna.debugger.bistoury.instrument.client.debugger.bean.BreakpointSnapshot

/**
 * 断点的快照信息被移除的监听器
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/30
 *
 * @see BreakpointSnapshotCache
 */
fun interface BreakpointSnapshotRemovalListener {

    /**
     * 当断点的快照信息被移除时, 需要执行的操作
     *
     * @param breakpointId 断点ID
     * @param snapshot 被移除的快照信息
     */

    fun onRemoval(breakpointId: String, snapshot: BreakpointSnapshot)
}