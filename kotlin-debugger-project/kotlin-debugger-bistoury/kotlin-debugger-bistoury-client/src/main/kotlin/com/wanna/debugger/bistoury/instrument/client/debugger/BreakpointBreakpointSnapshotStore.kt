package com.wanna.debugger.bistoury.instrument.client.debugger

/**
 * 断点的快照信息的保存的仓库
 *
 * * 1.在[BreakpointSnapshotReceiver]当中提供了对于快照的保存的相关方法
 * * 2.在[BreakpointSnapshotCache]当中提供了对于快照的获取的相关方法
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 *
 * @see DefaultBreakpointBreakpointSnapshotStore
 */
interface BreakpointBreakpointSnapshotStore : BreakpointSnapshotReceiver, BreakpointSnapshotCache