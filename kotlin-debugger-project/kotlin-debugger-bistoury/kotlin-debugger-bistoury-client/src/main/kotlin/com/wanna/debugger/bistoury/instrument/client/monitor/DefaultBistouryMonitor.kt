package com.wanna.debugger.bistoury.instrument.client.monitor

import com.wanna.debugger.bistoury.instrument.client.common.ClassFileBuffer
import com.wanna.debugger.bistoury.instrument.client.common.InstrumentInfo
import com.wanna.debugger.bistoury.instrument.client.location.ClassPathLookup
import com.wanna.debugger.bistoury.instrument.client.location.ResolvedSourceLocation
import java.lang.instrument.Instrumentation
import java.util.concurrent.locks.Lock
import javax.annotation.Nullable

/**
 * [BistouryMonitor]的默认实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/31
 */
open class DefaultBistouryMonitor : BistouryMonitor {

    /**
     * 类的字节码的缓存
     */
    @Nullable
    private var classFileBuffer: ClassFileBuffer? = null

    /**
     * Lock
     */
    @Nullable
    private var lock: Lock? = null

    /**
     * JVM回调的Instrumentation
     */
    @Nullable
    private var instrumentation: Instrumentation? = null

    /**
     * InstrumentInfo
     */
    @Nullable
    private var instrumentInfo: InstrumentInfo? = null

    /**
     * ClassPathLookup
     */
    @Nullable
    private var classPathLookup: ClassPathLookup? = null

    /**
     * 启动当前[BistouryMonitor], 提供动态监控相关功能的初始化
     *
     * @param instrumentInfo InstrumentInfo
     */
    override fun startup(instrumentInfo: InstrumentInfo): Boolean {
        this.classFileBuffer = instrumentInfo.classFileBuffer
        this.lock = instrumentInfo.lock
        this.instrumentation = instrumentInfo.instrumentation
        this.instrumentInfo = instrumentInfo

        // 创建ClassPathLookup
        this.classPathLookup = createClassPathLookup(instrumentInfo)
        return true
    }

    private fun createClassPathLookup(instrumentInfo: InstrumentInfo): ClassPathLookup {
        return ClassPathLookup(false, instrumentInfo.getClassPath().toTypedArray())
    }

    /**
     * 给类当中的某一行代码去添加动态监控
     *
     * @param sourceJavaFile 要去添加监控的类的Java文件路径(例如"com/wanna/Test.java")
     * @param lineNumber 要去添加监控的代码行号
     * @return 添加成功的监控的monitorId
     */
    override fun addMonitor(sourceJavaFile: String, lineNumber: Int): String {
        val location = classPathLookup?.resolveSourceLocation(sourceJavaFile, lineNumber)
            ?: throw IllegalStateException("ClassPathLookup is not available")

        // 如果这个位置已经添加过动态监控了, 那么直接return...
        if (BistouryGlobalMonitorContext.check(location)) {
            return "success"
        } else {
            BistouryGlobalMonitorContext.addMonitor(location)
        }

        // 执行目标字节码的增强
        val success = instrument(sourceJavaFile, lineNumber, location)
        if (success) {
            return "success"
        }
        return "failed"
    }

    /**
     * 执行对于目标类的字节码转换, 对目标类当中的某个方法的代码去进行增强
     *
     * @param sourceJavaFile 要去添加监控的类的Java文件路径(例如"com/wanna/Test.java")
     * @param lineNumber 要去添加监控的代码行号
     * @param location 源码所处的代码位置信息
     * @return 如果字节码转换成功return true
     */
    private fun instrument(sourceJavaFile: String, lineNumber: Int, location: ResolvedSourceLocation): Boolean {
        // 创建一个提供动态监控的Class文件转换器
        val monitorClassFileTransformer =
            BistouryMonitorClassFileTransformer(getClassFileBuffer(), sourceJavaFile, lineNumber, location)
        try {
            // 根据Instrumentation获取到JVM当中所有的类, 并获取到debugClass对应的类
            val clazz = getInstrumentInfo().signatureToClass(location.classSignature)

            // 将Monitor的ClassFileTransformer去添加到Instrumentation当中
            getInstrumentation().addTransformer(monitorClassFileTransformer, true)

            // 执行对于要对外提供监控功能的类, 去进行重新转换, 去进行类的转换
            getInstrumentation().retransformClasses(clazz)

            // 添加已经转换的类
            getInstrumentInfo().addTransformedClass(clazz)
        } finally {

            // 在完成Monitor的类的转换之后, 需要去移除Monitor的ClassFileTransformer
            getInstrumentation().removeTransformer(monitorClassFileTransformer)
        }

        return false
    }

    override fun removeMonitor(sourceJavaFile: String, lineNumber: Int, monitorId: String) {
        // Note: 不支持去进行Monitor的删除...
    }

    /**
     * 摧毁当前Monitor对象时, 将MonitorContext当中的缓存去进行清除
     */
    override fun destroy() {
        getLock().unlock()
        try {
            BistouryGlobalMonitorContext.destroy()
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
     * 获取到操作的锁
     *
     * @return Lock
     * @throws IllegalStateException 如果锁还没完成初始化的话
     */
    private fun getLock(): Lock = this.lock ?: throw IllegalStateException("Lock is not available")

    /**
     * 获取[ClassFileBuffer]
     *
     * @return ClassFileBuffer
     */
    private fun getClassFileBuffer(): ClassFileBuffer =
        this.classFileBuffer ?: throw IllegalStateException("ClassFileBuffer is not available")
}