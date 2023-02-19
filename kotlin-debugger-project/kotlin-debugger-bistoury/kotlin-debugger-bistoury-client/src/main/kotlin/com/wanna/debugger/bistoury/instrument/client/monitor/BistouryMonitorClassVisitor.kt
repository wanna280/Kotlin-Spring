package com.wanna.debugger.bistoury.instrument.client.monitor

import com.wanna.debugger.bistoury.instrument.client.common.AsmVersions
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AnalyzerAdapter
import org.objectweb.asm.commons.LocalVariablesSorter
import javax.annotation.Nullable

/**
 * 对需要添加Monitor动态监控的类去进行字节码的增强的ClassVisitor
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/2/1
 */
open class BistouryMonitorClassVisitor(
    classVisitor: ClassVisitor,
    private val methodName: String,
    private val methodDescriptor: String
) : ClassVisitor(AsmVersions.ASM_VERSION, classVisitor) {

    /**
     * className, 记录正在去进行访问的目标类
     */
    private var className: String? = null

    /**
     * 当访问这个类时, 我们需要去统计出来当前正在进行访问的类名
     */
    override fun visit(
        version: Int,
        access: Int,
        name: String,
        @Nullable signature: String?,
        @Nullable superName: String?,
        @Nullable interfaces: Array<String>?
    ) {
        this.className = name
        super.visit(version, access, name, signature, superName, interfaces)
    }

    /**
     * 访问类当中的方法时, 我们需要为目标要去进行监控的方法去生成相关的增强字节码
     *
     * @param access 当前正在访问的方法的访问修饰符
     * @param name 方法名
     * @param descriptor 方法的描述符
     * @param signature 方法签名
     * @param exceptions 正在访问的方法的异常表
     */
    @Nullable
    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        @Nullable signature: String?,
        @Nullable exceptions: Array<String>?
    ): MethodVisitor? {
        val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        // 只有methodName和methodDescriptor都匹配, 说明当前是我们想要去进行增强的目标方法 我们才需要去进行字节码的生成...
        if (methodName == name && descriptor == methodDescriptor) {
            val monitorMethodVisitor =
                BistouryMonitorMethodVisitor(methodVisitor, access, methodName, methodDescriptor, className!!)

            val analyzerAdapter = AnalyzerAdapter(className, access, name, descriptor, monitorMethodVisitor)
            monitorMethodVisitor.analyzerAdapter = analyzerAdapter

            val localVariablesSorter = LocalVariablesSorter(access, descriptor, analyzerAdapter)
            monitorMethodVisitor.localVariablesSorter = localVariablesSorter
            return localVariablesSorter
        } else {
            return methodVisitor
        }
    }
}