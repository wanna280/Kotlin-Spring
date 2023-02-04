package com.wanna.debugger.bistoury.instrument.client.monitor

import com.wanna.debugger.bistoury.instrument.client.common.AsmVersions
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AnalyzerAdapter
import org.objectweb.asm.commons.LocalVariablesSorter
import kotlin.math.max

/**
 * 基于ASM的方式去为Monitor动态监控提供字节码增强的[MethodVisitor]
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/1
 */
open class BistouryMonitorMethodVisitor(
    methodVisitor: MethodVisitor,
    private val access: Int,
    private val methodName: String,
    private val methodDescriptor: String,
    private val className: String
) : MethodVisitor(AsmVersions.ASM_VERSION, methodVisitor) {

    companion object {

        /**
         * Spy Name
         */
        private const val SPY_NAME = "com/wanna/debugger/bistoury/instrument/spy/BistourySpy"

        /**
         * Throwable Name
         */
        private const val THROWABLE_NAME = "java/lang/Throwable"

        /**
         * 开始监控的方法名
         */
        private const val START_MONITOR_METHOD_NAME = "start"

        /**
         * 开始监控的方法的描述符为"()J", 代表方法无参数, 返回值为long(注意不是Long, Descriptor当中long和Long一定得区分开)
         */
        private const val START_MONITOR_METHOD_DESCRIPTOR = "()J"

        /**
         * 结束监控的方法名
         */
        private const val STOP_MONITOR_METHOD_NAME = "stop";

        /**
         * 结束监控的方法的描述符为"(Ljava/lang/String;J)V", 代表方法参数为(String,long), 返回值为void
         */
        private const val STOP_MONITOR_METHOD_DESCRIPTOR = "(Ljava/lang/String;J)V"

        /**
         * 异常监控的方法名
         */
        private const val EXCEPTION_MONITOR_METHOD_NAME = "exception"

        /**
         * 异常监控的方法的描述符为"(Ljava/lang/String;)V", 代表方法参数为(String), 返回值为void
         */
        private const val EXCEPTION_MONITOR_METHOD_DESCRIPTOR = "(Ljava/lang/String;)V"

    }

    /**
     * MonitorKey
     */
    private val monitorKey = createMonitorKey()

    var analyzerAdapter: AnalyzerAdapter? = null

    var localVariablesSorter: LocalVariablesSorter? = null

    private val beginLabel = Label()

    private val endLabel = Label()

    private val throwableLabel = Label()

    /**
     * startTime所存放的局部变量表的槽位Index
     */
    private var startTimeLocalVariableIndex = 0

    private var maxStack = 0

    /**
     * 创建MonitorKey, 将会使用生成的去作为监控指标Key, 生成的格式为"{className}#{methodName}(params)"
     *
     * @return monitorKey
     */
    private fun createMonitorKey(): String {
        val builder = StringBuilder(className.replace("\\/", "."))
            .append('#').append(methodName).append('(')

        for (argumentType in Type.getMethodType(methodDescriptor).argumentTypes) {
            builder.append(argumentType.className).append(',')
        }
        if (builder.endsWith(',')) {
            builder.setLength(builder.length - 1)
        }
        builder.append(')')
        return builder.toString()
    }

    /**
     * 当访问到要去进行动态监控的目标方法的Code时, 我们需要执行字节码的增强
     */
    override fun visitCode() {
        // call super, use super to visitCode
        super.visitCode()

        // catch
        super.visitTryCatchBlock(beginLabel, endLabel, throwableLabel, THROWABLE_NAME)

        // start Monitor
        startMonitor()

        super.visitLabel(beginLabel)
    }

    /**
     * 当访问方法当中的一条字节码时, 我们需要检查是否遇到了return字节码?
     *
     * @param opcode 字节码OpCode
     */
    override fun visitInsn(opcode: Int) {
        // 从ireturn到return这个范围内的字节码, 都算是方法的返回...需要结束监控指标的记录
        if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
            endMonitor()

            // 重新计算maxStack...
            val stack = analyzerAdapter!!.stack
            if (stack == null) {
                this.maxStack = max(4, maxStack)
            } else {
                this.maxStack = max(stack.size + 4, maxStack)
            }
        }

        super.visitInsn(opcode)
    }

    override fun visitMaxs(maxStack: Int, maxLocals: Int) {
        super.visitLabel(endLabel)

        emitCatchBlocks()

        super.visitMaxs(max(maxStack, this.maxStack), maxLocals + 1)
    }

    private fun emitCatchBlocks() {
        super.visitLabel(throwableLabel)

        val exceptionLocalVariableIndex = localVariablesSorter!!.newLocal(Type.getType(Any::class.java))

        super.visitVarInsn(Opcodes.ASTORE, exceptionLocalVariableIndex)

        endMonitor()
        exceptionMonitor()

        super.visitVarInsn(Opcodes.ALOAD, exceptionLocalVariableIndex)
        super.visitInsn(Opcodes.ATHROW)
    }

    /**
     * 开始记录监控, 对应的代码如下
     *
     * ```kotlin
     * val startTime = Bistoury.start()
     * ```
     */
    private fun startMonitor() {
        // 执行Bistoury.start方法, 并将该方法的返回值, 弹回到当前方法栈当中来
        super.visitMethodInsn(
            Opcodes.INVOKESTATIC, SPY_NAME, START_MONITOR_METHOD_NAME, START_MONITOR_METHOD_DESCRIPTOR, false
        )

        // 尝试获取到下一个局部变量表槽位的index, 这个位置应该需要存放startTime
        this.startTimeLocalVariableIndex = localVariablesSorter!!.newLocal(Type.LONG_TYPE)

        // 使用lstore字节码, 将startTime存到局部变量表当中
        super.visitVarInsn(Opcodes.LSTORE, startTimeLocalVariableIndex)

        this.maxStack = 4
    }

    /**
     * 结束监控的记录, 对应的代码如下
     *
     * ```kotlin
     * BistourySpy.stop(monitorKey, startTime)
     * ```
     */
    private fun endMonitor() {
        super.visitLdcInsn(monitorKey)

        // 使用lload指令, 从局部变量表当中去进行加载得到startTime
        super.visitVarInsn(Opcodes.LLOAD, startTimeLocalVariableIndex)

        super.visitMethodInsn(
            Opcodes.INVOKESTATIC, SPY_NAME, STOP_MONITOR_METHOD_NAME, STOP_MONITOR_METHOD_DESCRIPTOR, false
        )
    }

    /**
     * 添加记录异常监控的字节码, 对应的代码如下
     *
     * ```kotlin
     * BistourySpy.exception(monitorKey)
     * ```
     */
    private fun exceptionMonitor() {
        super.visitLdcInsn(monitorKey)
        super.visitMethodInsn(
            Opcodes.INVOKESTATIC, SPY_NAME, EXCEPTION_MONITOR_METHOD_NAME, EXCEPTION_MONITOR_METHOD_DESCRIPTOR, false
        )
    }
}