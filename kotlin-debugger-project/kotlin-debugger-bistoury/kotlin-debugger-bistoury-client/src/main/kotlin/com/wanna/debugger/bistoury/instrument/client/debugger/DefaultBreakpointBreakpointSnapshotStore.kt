package com.wanna.debugger.bistoury.instrument.client.debugger

import com.wanna.debugger.bistoury.instrument.client.debugger.bean.BreakpointSnapshot
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.Lock
import javax.annotation.Nullable

/**
 * 默认的断点的快照信息的缓存实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 *
 * @param lock 执行清理缓存的操作时需要用到的锁
 * @param removalListener 当断点的快照信息被移除时需要执行的操作的回调函数Listener
 * @param cleanCacheExecutor 执行对于断点的缓存的清理的异步线程池
 */
open class DefaultBreakpointBreakpointSnapshotStore(
    private val lock: Lock,
    private val removalListener: BreakpointSnapshotRemovalListener,
    private val cleanCacheExecutor: ScheduledExecutorService
) : BreakpointBreakpointSnapshotStore {

    companion object {
        /**
         * 缓存过期时间(分钟)
         */
        private const val EXPIRE_MINUTES = 10L

        /**
         * 定时任务检查的间隔时间
         */
        private const val PERIOD_MINUTES = 1L
    }


    /**
     * 维护断点的快照信息, Key-断点ID(breakpointId), Value-该断点位置的快照信息
     */
    private val breakpointSnapshotCache = ConcurrentHashMap<String, BreakpointSnapshot>()

    /**
     * 断点快照的缓存的过期时间(10分钟)
     */
    private val expireTime = TimeUnit.MINUTES.toMillis(EXPIRE_MINUTES)

    init {
        // 启动定时任务, 10分钟之后, 每隔一分钟去检查一下该断点是否过期?
        cleanCacheExecutor.scheduleAtFixedRate({
            for (breakpointId in breakpointSnapshotCache.keys()) {
                cleanCache(breakpointId)
            }
        }, EXPIRE_MINUTES, PERIOD_MINUTES, TimeUnit.MINUTES)
    }

    /**
     * 刷新断点的过期时间
     *
     * @param breakpointId 断点ID
     */
    override fun refreshBreakpointExpireTime(breakpointId: String) {
        val snapshot = breakpointSnapshotCache[breakpointId] ?: return
        snapshot.expireTime = computeExpireTime()
    }

    /**
     * 重新计算过期时间
     *
     * @return 新的过期时间的时间戳
     */
    private fun computeExpireTime(): Long = System.currentTimeMillis() + expireTime

    override fun initBreakPoint(breakpointId: String, sourceJavaFile: String, lineNumber: Int) {
        breakpointSnapshotCache[breakpointId] =
            BreakpointSnapshot(breakpointId, sourceJavaFile, lineNumber, computeExpireTime())
    }

    override fun putLocalVariables(breakpointId: String, localVariables: Map<String, Any?>) {
        val snapshot = breakpointSnapshotCache[breakpointId] ?: return
        snapshot.localVariables = localVariables
    }

    override fun putFields(breakpointId: String, fields: Map<String, Any?>) {
        val snapshot = breakpointSnapshotCache[breakpointId] ?: return
        snapshot.fields = fields
    }

    override fun putStaticFields(breakpointId: String, staticFields: Map<String, Any?>) {
        val snapshot = breakpointSnapshotCache[breakpointId] ?: return
        snapshot.staticFields = staticFields
    }

    override fun fillStacktrace(breakpointId: String, stackTrace: Array<StackTraceElement>) {
        val snapshot = breakpointSnapshotCache[breakpointId] ?: return
        snapshot.stackTrace = stackTrace
    }

    override fun setSourceClass(breakpointId: String, sourceJavaFile: String) {

    }

    override fun setLineNumber(breakpointId: String, lineNumber: Int) {

    }

    override fun endReceive(breakpointId: String) {

    }

    override fun endFail(breakpointId: String) {

    }

    @Nullable
    override fun getBreakpointSnapshot(breakpointId: String): BreakpointSnapshot? {
        return breakpointSnapshotCache[breakpointId]
    }


    override fun removeBreakpointSnapshot(breakpointId: String) {
        breakpointSnapshotCache.remove(breakpointId)
    }

    private fun cleanCache(breakpointId: String) {
        lock.lock()
        try {
            val snapshot = breakpointSnapshotCache[breakpointId]
            if (snapshot != null && System.currentTimeMillis() >= snapshot.expireTime) {
                breakpointSnapshotCache.remove(breakpointId)

                // callback监听移除事件的Listener
                removalListener.onRemoval(breakpointId, snapshot)
            }
        } finally {
            lock.unlock()
        }
    }
}