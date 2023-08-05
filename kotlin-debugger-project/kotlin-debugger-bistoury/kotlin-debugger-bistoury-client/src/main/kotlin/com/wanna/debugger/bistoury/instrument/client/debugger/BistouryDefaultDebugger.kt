package com.wanna.debugger.bistoury.instrument.client.debugger

import com.wanna.debugger.bistoury.instrument.client.common.InstrumentInfo
import com.wanna.debugger.bistoury.instrument.client.location.ClassPathLookup
import com.wanna.debugger.bistoury.instrument.client.location.Location
import com.wanna.debugger.bistoury.instrument.client.location.ResolvedSourceLocation
import java.lang.instrument.Instrumentation
import java.util.concurrent.locks.Lock
import javax.annotation.Nullable

/**
 * [BistouryDebugger]的默认实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 */
open class BistouryDefaultDebugger : BistouryDebugger {

    /**
     * InstrumentInfo
     */
    @Nullable
    private var instrumentInfo: InstrumentInfo? = null

    /**
     * Instrumentation
     */
    @Nullable
    private var instrumentation: Instrumentation? = null

    /**
     * 已经增强过的代码位置缓存, 避免去进行重复的增强
     */
    private val instrumented = LinkedHashSet<Location>()

    /**
     * Lock
     */
    @Nullable
    private var lock: Lock? = null

    /**
     * BreakpointSnapshot Receiver
     */
    @Nullable
    private var receiver: BreakpointSnapshotReceiver? = null

    /**
     * ClassPathLookup
     */
    @Nullable
    private var classPathLookup: ClassPathLookup? = null

    /**
     * 启动[BistouryDebugger], 完成相关初始化工作
     *
     * @param instrumentInfo InstrumentInfo
     * @param receiver 用于去进行保存快照的缓存的回调函数
     */
    override fun startup(instrumentInfo: InstrumentInfo, receiver: BreakpointSnapshotReceiver) {
        this.instrumentInfo = instrumentInfo
        this.instrumentation = instrumentInfo.instrumentation
        this.lock = instrumentInfo.lock
        this.receiver = receiver

        // 创建ClassPathLookup
        this.classPathLookup = createClassPathLookup(instrumentInfo)


        // 将SnapshotReceiver去保存到GlobalDebugContext当中
        BistouryGlobalDebugContext.initSnapshotReceiver(receiver)
    }

    private fun createClassPathLookup(instrumentInfo: InstrumentInfo): ClassPathLookup {
        return ClassPathLookup(false, instrumentInfo.getClassPath().toTypedArray())
    }

    /**
     * 给指定的类的某个代码行, 去添加一个断点, 为目标类的目标方法, 去添加相应的增强字节码...
     *
     * @param sourceJavaFile 要去进行打断点的类的Java文件路径(例如"com/wanna/Test.java")
     * @param lineNumber 要去进行打断点的源码行号
     * @param breakpointCondition 断点条件
     * @return 注册成功的断点ID
     */
    override fun registerBreakpoint(sourceJavaFile: String, lineNumber: Int, breakpointCondition: String): String {
        getLock().lock()
        try {
            return doRegisterBreakpoint(sourceJavaFile, lineNumber, breakpointCondition)
        } finally {
            getLock().unlock()
        }
    }

    /**
     * 在有锁的情况下, 真正去进行断点的添加
     *
     * @param sourceJavaFile 要去进行打断点的类的Java文件路径(例如"com/wanna/Test.java")
     * @param lineNumber 要去进行打断点的源码行号
     * @param breakpointCondition 断点条件
     * @return 注册的断点的ID
     */
    private fun doRegisterBreakpoint(sourceJavaFile: String, lineNumber: Int, breakpointCondition: String): String {
        val resolvedLocation = classPathLookup?.resolveSourceLocation(sourceJavaFile, lineNumber)
            ?: throw IllegalStateException("ClassPathLookup is not available")

        val codeLocation = Location(sourceJavaFile, resolvedLocation.adjustedLineNumber)
        val receiver = getSnapshotReceiver()
        val (breakpointId, newId) = BistouryGlobalDebugContext.addBreakpoint(codeLocation, breakpointCondition)

        // 如果不是一个新的breakpointId, 那么需要去刷新断点的过期时间
        if (!newId) {
            receiver.refreshBreakpointExpireTime(breakpointId)
            return breakpointId
        }

        // 如果是一个新的breakpointId的话, 需要去对执行目标类的增强...
        val success = instrument(sourceJavaFile, codeLocation, resolvedLocation)
        if (success) {
            receiver.initBreakPoint(breakpointId, codeLocation.sourceJavaFile, codeLocation.lineNumber)
        } else {
            BistouryGlobalDebugContext.removeBreakpoint(codeLocation, breakpointId)
        }
        return breakpointId
    }

    /**
     * 给指定的类的某个代码行上的断点, 去进行取消注册(移除)一个断点
     *
     * @param sourceJavaFile 要去取消注册断点的类的Java文件路径(例如"com/wanna/Test.java")
     * @param lineNumber 要去取消注册断点的类的源码行
     * @param breakpointId 要去取消断点的断点ID
     */
    override fun deregisterBreakpoint(sourceJavaFile: String, lineNumber: Int, breakpointId: String) {
        getLock().lock()
        try {
            // remove Breakpoint
            BistouryGlobalDebugContext.removeBreakpoint(sourceJavaFile, lineNumber, breakpointId)
        } finally {
            getLock().unlock()
        }
    }

    /**
     * 执行对于目标类的字节码增强
     *
     * @param sourceClassFile 要去进行打断点的类的Java文件路径(例如"com/wanna/Test.java")
     * @param realLocation 代码位置信息
     */
    private fun instrument(
        sourceClassFile: String,
        realLocation: Location,
        sourceLocation: ResolvedSourceLocation
    ): Boolean {
        if (instrumented.contains(realLocation)) {
            return true
        }
        val transformer =
            BistouryDebuggerClassFileTransformer(getInstrumentInfo().classFileBuffer, sourceClassFile, sourceLocation)

        try {
            // 从所有已经加载的类当中, 使用Instrumentation去根据className去进行获取到合适的类
            val clazz = getInstrumentInfo().signatureToClass(sourceLocation.classSignature)

            // 添加ClassFileTransformer, 去对Class文件去进行转换
            getInstrumentation().addTransformer(transformer, true)

            // 利用ClassFileTransformer重新去对已经有的类去进行增强...
            getInstrumentation().retransformClasses(clazz)
            instrumented += realLocation

            // 将该类添加到已经转换的列表当中
            getInstrumentInfo().addTransformedClass(clazz)

            return true
        } finally {

            // 在完成对于这个类的转换之后, 移除掉刚添加的Transformer...
            getInstrumentation().removeTransformer(transformer)
        }
    }

    /**
     * 摧毁当前[BistouryDebugger]
     *
     * @see BistouryGlobalDebugContext.destroy
     */
    override fun destroy() {
        getLock().lock()
        try {
            // destroy BistouryGlobalDebugContext
            BistouryGlobalDebugContext.destroy()
        } finally {
            getLock().unlock()
        }
    }

    /**
     * 获取[InstrumentInfo]
     *
     * @return InstrumentInfo
     * @throws IllegalStateException 如果InstrumentInfo还没完成初始化
     */
    private fun getInstrumentInfo(): InstrumentInfo =
        this.instrumentInfo ?: throw IllegalStateException("InstrumentInfo is not available")

    /**
     * 获取[Instrumentation]
     *
     * @return Instrumentation
     * @throws IllegalStateException 如果Instrumentation还没完成初始化
     */
    private fun getInstrumentation(): Instrumentation =
        this.instrumentation ?: throw IllegalStateException("Instrumentation is not available")

    /**
     * 获取[BreakpointSnapshotReceiver]
     *
     * @return BreakpointSnapshotReceiver
     * @throws IllegalStateException 如果SnapshotReceiver还没完成初始化
     */
    private fun getSnapshotReceiver(): BreakpointSnapshotReceiver =
        this.receiver ?: throw IllegalStateException("BreakpointSnapshotReceiver is not available")

    /**
     * 获取到操作的锁
     *
     * @return Lock
     * @throws IllegalStateException 如果锁还没完成初始化的话
     */
    private fun getLock(): Lock = this.lock ?: throw IllegalStateException("Lock is not available")
}