package com.wanna.debugger.bistoury.instrument.agent

import com.wanna.debugger.bistoury.instrument.spy.BistourySpy
import com.wanna.debugger.bistoury.instrument.spy.BistourySpy.DUMP_METHOD_NAME
import com.wanna.debugger.bistoury.instrument.spy.BistourySpy.END_RECEIVE_METHOD_NAME
import com.wanna.debugger.bistoury.instrument.spy.BistourySpy.EXCEPTION_MONITOR_METHOD_NAME
import com.wanna.debugger.bistoury.instrument.spy.BistourySpy.FILL_STACK_TRACE_METHOD_NAME
import com.wanna.debugger.bistoury.instrument.spy.BistourySpy.HAS_BREAK_POINT_SET_METHOD_NAME
import com.wanna.debugger.bistoury.instrument.spy.BistourySpy.IS_HIT_BREAKPOINT_METHOD_NAME
import com.wanna.debugger.bistoury.instrument.spy.BistourySpy.PUT_FIELD_METHOD_NAME
import com.wanna.debugger.bistoury.instrument.spy.BistourySpy.PUT_LOCAL_VARIABLE_METHOD_NAME
import com.wanna.debugger.bistoury.instrument.spy.BistourySpy.PUT_STATIC_METHOD_NAME
import com.wanna.debugger.bistoury.instrument.spy.BistourySpy.START_MONITOR_METHOD_NAME
import com.wanna.debugger.bistoury.instrument.spy.BistourySpy.STOP_MONITOR_METHOD_NAME
import java.lang.instrument.Instrumentation
import javax.annotation.Nullable

/**
 * Bistoury JavaAgent Bootstrap
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 */
object AgentBootstrap {

    /**
     * 全局的DebugContext的类的类名
     */
    private const val GLOBAL_DEBUG_CONTEXT_CLASS_NAME =
        "com.wanna.debugger.bistoury.instrument.client.debugger.BistouryGlobalDebugContext"

    /**
     * 基于ThreadLocal的方式去实现快照的捕捉的类的类名
     */
    private const val SNAPSHOT_CAPTURE_CLASS_NAME =
        "com.wanna.debugger.bistoury.instrument.client.debugger.BreakpointSnapshotCapture"

    /**
     * Monitor动态监控的类名
     */
    private const val AGENT_MONITOR_CLASS_NAME =
        "com.wanna.debugger.bistoury.instrument.client.monitor.BistouryAgentMonitor"

    /**
     * BistouryBootstrap的类名
     */
    private const val BISTOURY_BOOTSTRAP_CLASS_NAME =
        "com.wanna.debugger.bistoury.instrument.client.common.BistouryBootstrap"

    /**
     * BistouryBootstrap类的初始化方法
     */
    private const val BISTOURY_BOOTSTRAP_INIT_METHOD_NAME = "init"

    /**
     * 检查BistouryBootstrap是否已经完成了绑定工作, 需要用到的方法的方法名
     */
    private const val BISTOURY_BOOTSTRAP_IS_BOUND_METHOD_NAME = "isBound"

    /**
     * BistouryBootstrap的绑定的方法的方法名
     */
    private const val BISTOURY_BOOTSTRAP_BIND_METHOD_NAME = "bind"

    /**
     * JVM自动回调的[Instrumentation]
     */
    @JvmStatic
    private var instrumentation: Instrumentation? = null

    @JvmStatic
    fun premain(@Nullable args: String?, instrumentation: Instrumentation) {
        this.instrumentation = instrumentation

        main(args, instrumentation)
    }

    @JvmStatic
    fun agentmain(@Nullable args: String?, instrumentation: Instrumentation) {
        this.instrumentation = instrumentation

        main(args, instrumentation)
    }

    @JvmStatic
    private fun main(@Nullable args: String?, instrumentation: Instrumentation) {

        // 初始化BistourySpy
        initBistourySpy(ClassLoader.getSystemClassLoader())

        // 完成BistouryBootstrap的绑定
        bind(instrumentation, ClassLoader.getSystemClassLoader(), args)
    }

    /**
     * 初始化BistourySpy, 对于BistourySpy当中的方法, 将会被ASM的放上去插入字节码去进行调用...
     *
     * @param classLoader 执行类加载的ClassLoader
     * @see BistourySpy
     */
    @JvmStatic
    private fun initBistourySpy(classLoader: ClassLoader) {
        val debugContextClass = classLoader.loadClass(GLOBAL_DEBUG_CONTEXT_CLASS_NAME)
        val snapshotCaptureClass = classLoader.loadClass(SNAPSHOT_CAPTURE_CLASS_NAME)
        val agentMonitorClass = classLoader.loadClass(AGENT_MONITOR_CLASS_NAME)

        // 初始化Bistoury Spy
        BistourySpy.init(
            // 检查是否有断点的方法(className, lineNumber)
            debugContextClass.getDeclaredMethod(HAS_BREAK_POINT_SET_METHOD_NAME, String::class.java, Int::class.java),

            // 检查是否命中断点的方法(className, lineNumber)
            debugContextClass.getDeclaredMethod(IS_HIT_BREAKPOINT_METHOD_NAME, String::class.java, Int::class.java),

            // 添加局部变量快照方法(name, value)
            snapshotCaptureClass.getDeclaredMethod(PUT_LOCAL_VARIABLE_METHOD_NAME, String::class.java, Any::class.java),

            // 添加成员变量字段快照方法(name, value)
            snapshotCaptureClass.getDeclaredMethod(PUT_FIELD_METHOD_NAME, String::class.java, Any::class.java),

            // 添加static变量字段快照方法(name, value)
            snapshotCaptureClass.getDeclaredMethod(PUT_STATIC_METHOD_NAME, String::class.java, Any::class.java),

            // 填充快照栈轨迹的方法(className, lineNumber, exception)
            snapshotCaptureClass.getDeclaredMethod(
                FILL_STACK_TRACE_METHOD_NAME, String::class.java, Int::class.java, Throwable::class.java
            ),

            // 保存快照的方法(className, lineNumber)
            snapshotCaptureClass.getDeclaredMethod(DUMP_METHOD_NAME, String::class.java, Int::class.java),

            // 结束断点快照的方法(className, lineNumber)
            snapshotCaptureClass.getDeclaredMethod(END_RECEIVE_METHOD_NAME, String::class.java, Int::class.java),

            // 开始记录监控的方法
            agentMonitorClass.getDeclaredMethod(START_MONITOR_METHOD_NAME),

            // 结束记录监控的方法(metricKey, startTime)
            agentMonitorClass.getDeclaredMethod(STOP_MONITOR_METHOD_NAME, String::class.java, Long::class.java),

            // 记录异常监控的方法(metricKey)
            agentMonitorClass.getDeclaredMethod(EXCEPTION_MONITOR_METHOD_NAME, String::class.java)
        )
    }

    /**
     * 完成BistouryBootstrap的绑定工作, 在bind方法当中会去启动各个InstrumentClient(其中就包含DebugClient)
     *
     * @param instrumentation Instrumentation
     * @param classLoader ClassLoader
     * @param args args
     */
    @JvmStatic
    private fun bind(instrumentation: Instrumentation, classLoader: ClassLoader, @Nullable args: String?) {
        val bootstrapClass = classLoader.loadClass(BISTOURY_BOOTSTRAP_CLASS_NAME)

        // 获取到BistouryBootstrap类的初始化方法, 并完成初始化
        val bootstrapInitMethod =
            bootstrapClass.getMethod(BISTOURY_BOOTSTRAP_INIT_METHOD_NAME, Int::class.java, Instrumentation::class.java)
        bootstrapInitMethod.invoke(null, 0, instrumentation)


        // 检查是否已经完成了绑定, 如果已经完成绑定, 那么不应该去进行重复绑定...
        val isBoundMethod = bootstrapClass.getDeclaredMethod(BISTOURY_BOOTSTRAP_IS_BOUND_METHOD_NAME)
        val isBound = isBoundMethod.invoke(null) as Boolean
        if (!isBound) {
            // 执行BistouryBootstrap类的bind方法完成绑定
            val bindMethod = bootstrapClass.getDeclaredMethod(BISTOURY_BOOTSTRAP_BIND_METHOD_NAME)
            bindMethod.invoke(null)
        }

    }
}