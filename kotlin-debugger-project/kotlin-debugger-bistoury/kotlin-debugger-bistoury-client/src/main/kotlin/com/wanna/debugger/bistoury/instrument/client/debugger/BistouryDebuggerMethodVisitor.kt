package com.wanna.debugger.bistoury.instrument.client.debugger

import com.wanna.debugger.bistoury.instrument.client.common.AsmVersions
import com.wanna.debugger.bistoury.instrument.client.debugger.bean.ClassMetadata
import com.wanna.debugger.bistoury.instrument.client.location.Location
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter
import javax.annotation.Nullable

/**
 * Debugger的方法增强器, 将设有动态断点的位置, 去插入对应的字节码
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 *
 * @see BistouryDebuggerClassVisitor
 *
 * @param sourceJavaFile 要去进行打断点的类的Java文件路径(例如"com/wanna/Test.java")
 */
open class BistouryDebuggerMethodVisitor(
    private val sourceJavaFile: String,
    private val className: String,
    private val methodName: String,
    private val desc: String,
    @Nullable originMethodVisitor: MethodVisitor?,
    access: Int,
    private val classMetadata: ClassMetadata
) : AdviceAdapter(AsmVersions.ASM_VERSION, originMethodVisitor, access, methodName, desc) {

    /**
     * methodId, 基于methodName和descriptor去进行拼接生成, 因为存在有方法重载的情况, 因此methodId必须使用方法名+方法参数去进行生成
     */
    private val methodId = ClassMetadata.createMethodId(methodName, desc)

    companion object {
        /**
         * Spy Name
         */
        private const val SPY_NAME = "com/wanna/debugger/bistoury/instrument/spy/BistourySpy"

        /**
         * Throwable Name
         */
        private const val THROWABLE_NAME = "java/lang/Throwable"
    }


    /**
     * 当访问其中一行的代码时, 我们需要去检查是否有断点, 如果有的话, 那么插入响应的字节码...
     *
     * @param lineNumber 代码的行号
     * @param start Label
     */
    override fun visitLineNumber(lineNumber: Int, start: Label) {
        super.visitLineNumber(lineNumber, start)

        // 根据sourceClassName&lineNumber构建出来Location, 去描述某个类当中的某个具体的代码行
        val location = Location(sourceJavaFile, lineNumber)

        // 检查给定的源码位置这个地方是否有断点设置...
        if (BistouryGlobalDebugContext.hasBreakpointSet(location)) {
            val breakpointLabel = Label()

            // 在这里去插入"检查该位置的断点的断点开关是否打开"的字节码
            breakpointSwitch(sourceJavaFile, lineNumber, breakpointLabel)

            // 在这里去插入:用于去进行捕捉快照"的字节码
            captureSnapshot(lineNumber)

            // 在这里去插入"检查是否命中断点"的字节码...
            isHitBreakpoint(sourceJavaFile, lineNumber, breakpointLabel)

            // 在这里去插入添加"真正去处理断点"的字节码
            processForBreakpoint(sourceJavaFile, lineNumber)

            super.visitLabel(breakpointLabel)
        }
    }

    /**
     * 插入检查该位置的断点是否开启的字节码, 对应下面的代码
     *
     * ```kotlin
     * if(BistourySpy.hasBreakpointSet(source, lineNumber)) {
     *
     * }
     * ```
     */
    private fun breakpointSwitch(source: String, lineNumber: Int, breakpointLabel: Label) {
        super.visitLdcInsn(source)
        super.visitLdcInsn(lineNumber)
        super.visitMethodInsn(INVOKESTATIC, SPY_NAME, "hasBreakpointSet", "(Ljava/lang/String;I)Z", false)
        super.visitJumpInsn(IFEQ, breakpointLabel)
    }

    /**
     * 添加捕捉localVariables/fields/staticFields这些信息的快照信息的字节码
     *
     * @param lineNumber 断点对应的代码行号
     */
    private fun captureSnapshot(lineNumber: Int) {
        addLocalVariables(lineNumber)
        addStaticFields(lineNumber)
        addFields(lineNumber)
    }

    /**
     * 添加收集局部变量表当中的变量的相关信息的字节码, 对应的是下面的代码
     *
     * ```kotlin
     * for(variable in localVariables) {
     *    BistourySpy.putLocalVariable(variable.name, variable.value)
     * }
     * ```
     *
     * @param lineNumber 行号
     */
    private fun addLocalVariables(lineNumber: Int) {
        val localVariables = classMetadata.localVariables[methodId] ?: return
        for (localVariable in localVariables) {
            if (localVariable.start <= lineNumber && lineNumber <= localVariable.end) {
                super.visitLdcInsn(localVariable.name)
                super.visitVarInsn(Type.getType(localVariable.descriptor).getOpcode(ILOAD), localVariable.index)
                boxIfNecessary(localVariable.descriptor)
                super.visitMethodInsn(
                    INVOKESTATIC, SPY_NAME, "putLocalVariable", "(Ljava/lang/String;Ljava/lang/Object;)V", false
                )
            }
        }
    }

    /**
     * 添加收集所有的static变量的相关信息的字节码, 对应的是下面的代码
     *
     * ```kotlin
     * for(staticField in staticFields) {
     *   BistourySpy.putStatic(staticField.name, staticField.value)
     * }
     * ```
     */
    private fun addStaticFields(lineNumber: Int) {
        for ((access, name, descriptor) in classMetadata.staticFields) {
            super.visitLdcInsn(name)
            super.visitFieldInsn(GETSTATIC, className, name, descriptor)
            boxIfNecessary(desc)

            super.visitMethodInsn(
                INVOKESTATIC, SPY_NAME, "putStaticField", "(Ljava/lang/String;Ljava/lang/Object;)V", false
            )
        }
    }

    /**
     * 添加收集所有的成员变量的相关信息的字节码, 对应的是下面的代码
     *
     * ```kotlin
     * for(field in fields) {
     *   BistourySpy.putField(field.name, field.value)
     * }
     * ```
     */
    private fun addFields(lineNumber: Int) {
        if ((access and Opcodes.ACC_STATIC) == 0) {
            for ((access, name, descriptor) in classMetadata.fields) {

                super.visitLdcInsn(name)
                super.visitVarInsn(ALOAD, 0)
                super.visitFieldInsn(GETFIELD, className, name, descriptor)
                boxIfNecessary(descriptor)

                super.visitMethodInsn(
                    INVOKESTATIC, SPY_NAME, "putField", "(Ljava/lang/String;Ljava/lang/Object;)V", false
                )
            }
        }

    }

    /**
     * 添加检查是否命中断点的字节码, 对应下面的代码
     *
     * ```kotlin
     * if(BistourySpy.isHitBreakpoint(source, lineNumber)) {
     *
     * }
     * ```
     *
     * @param source source
     * @param lineNumber lineNumber
     * @param breakpointLabel 断点标签
     */
    private fun isHitBreakpoint(source: String, lineNumber: Int, breakpointLabel: Label) {
        super.visitLdcInsn(source)
        super.visitLdcInsn(lineNumber)
        super.visitMethodInsn(INVOKESTATIC, SPY_NAME, "isHitBreakpoint", "(Ljava/lang/String;I)Z", false)
        super.visitJumpInsn(IFEQ, breakpointLabel)
    }

    /**
     * 添加处理断点的字节码
     *
     * @param source source
     * @param lineNumber 行号
     */
    private fun processForBreakpoint(source: String, lineNumber: Int) {
        val theEnd = Label()
        dump(source, lineNumber)
        fillStacktrace(source, lineNumber)
        endReceive(source, lineNumber)

        super.visitLabel(theEnd)
    }

    /**
     * 添加执行dump的字节码, 对应的代码如下
     *
     * ```kotlin
     * BistourySpy.dump(source, lineNumber)
     * ```
     *
     * @param source source
     * @param lineNumber 行号
     */
    private fun dump(source: String, lineNumber: Int) {
        super.visitLdcInsn(source)
        super.visitLdcInsn(lineNumber)
        super.visitMethodInsn(INVOKESTATIC, SPY_NAME, "dump", "(Ljava/lang/String;I)V", false)
    }

    /**
     * 添加填充栈轨迹的的字节码, 对应的代码如下
     *
     * ```kotlin
     * BistourySpy.fillStacktrace(source, lineNumber, new Throwable())
     * ```
     *
     * @param source source
     * @param lineNumber 行号
     */
    private fun fillStacktrace(source: String, lineNumber: Int) {
        super.visitLdcInsn(source)
        super.visitLdcInsn(lineNumber)

        // 创建一个Throwable对象
        super.visitTypeInsn(NEW, THROWABLE_NAME)
        super.visitInsn(DUP)
        super.visitMethodInsn(INVOKESPECIAL, THROWABLE_NAME, "<init>", "()V", false)

        super.visitMethodInsn(
            INVOKESTATIC,
            SPY_NAME,
            "fillStacktrace",
            "(Ljava/lang/String;ILjava/lang/Throwable;)V",
            false
        )
    }

    /**
     * 生成结束断点的字节码, 对应的代码如下
     * ```kotlin
     * BistourySpy.endReceive(source, lineNumber)
     * ```
     *
     * @param source source
     * @param lineNumber lineNumber
     */
    private fun endReceive(source: String, lineNumber: Int) {
        super.visitLdcInsn(source)
        super.visitLdcInsn(lineNumber)
        super.visitMethodInsn(INVOKESTATIC, SPY_NAME, "endReceive", "(Ljava/lang/String;I)V", false)
    }

    private fun boxIfNecessary(desc: String) {

    }

    override fun visitMaxs(maxStack: Int, maxLocals: Int) {
        // Note: 多预留点局部变量表槽位/栈槽位, 不然会遇到"org.objectweb.asm.tree.analysis.AnalyzerException: Error at instruction 3: Insufficient maximum stack size."
        super.visitMaxs(maxStack + 3, maxLocals)
    }
}