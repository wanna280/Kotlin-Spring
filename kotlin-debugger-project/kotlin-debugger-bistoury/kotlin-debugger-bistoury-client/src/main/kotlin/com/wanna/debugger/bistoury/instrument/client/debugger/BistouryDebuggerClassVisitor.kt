package com.wanna.debugger.bistoury.instrument.client.debugger

import com.wanna.debugger.bistoury.instrument.client.common.AsmVersions
import com.wanna.debugger.bistoury.instrument.client.debugger.bean.ClassMetadata
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import javax.annotation.Nullable

/**
 * 为Debugger断点去生成对应的字节码的[ClassVisitor], 当遇到断点时, 我们需要将快照信息去保存下来
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 *
 * @see BistouryDebuggerMethodVisitor
 *
 * @param classVisitor ClassVisitor
 * @param sourceJavaFile 要去进行打断点的类的Java文件路径(例如"com/wanna/Test.java")
 * @param classMetadata 要去进行添加字节码的类的元信息, 我们根据这个元信息去进行快照的保存
 */
open class BistouryDebuggerClassVisitor(
    private val classVisitor: ClassVisitor,
    private val sourceJavaFile: String,
    private val classMetadata: ClassMetadata
) : ClassVisitor(AsmVersions.ASM_VERSION, classVisitor) {

    /***
     * className
     */
    private var className: String? = null


    /**
     * 当访问类时, 我们需要将className去记录下来
     */
    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String?,
        interfaces: Array<String>?
    ) {
        this.className = name
        super.visit(version, access, name, signature, superName, interfaces)
    }

    /**
     * 当访问方法时, 我们需要去插入检查是否有断点的代码
     */
    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        @Nullable signature: String?,
        @Nullable exceptions: Array<String>?
    ): MethodVisitor {
        val methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)

        // 返回用于检查某一行的代码处是否有断点的检查的MethodVisitor
        return BistouryDebuggerMethodVisitor(
            sourceJavaFile, className!!, name, descriptor, methodVisitor, access, classMetadata
        )
    }
}