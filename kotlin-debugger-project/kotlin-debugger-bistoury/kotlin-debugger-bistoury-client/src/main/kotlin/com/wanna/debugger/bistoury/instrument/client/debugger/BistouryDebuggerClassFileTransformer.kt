package com.wanna.debugger.bistoury.instrument.client.debugger

import com.wanna.debugger.bistoury.instrument.client.common.BaseClassFileTransformer
import com.wanna.debugger.bistoury.instrument.client.common.ClassFileBuffer
import com.wanna.debugger.bistoury.instrument.client.debugger.bean.ClassMetadata
import com.wanna.debugger.bistoury.instrument.client.location.ResolvedSourceLocation
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Type
import org.objectweb.asm.util.CheckClassAdapter
import java.lang.instrument.ClassFileTransformer
import java.security.ProtectionDomain
import javax.annotation.Nullable

/**
 * Debugger的Class文件的转换器, 对要去进行Debug的目标类, 去进行字节码的增强(对于非debug类之外的别的类, 我们直接pass掉)
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/29
 *
 * @see ClassFileTransformer
 *
 * @param sourceJavaFile 要去进行打断点的类的Java文件路径(例如"com/wanna/Test.java")
 * @param classFileBuffer 类的字节码文件的缓存
 * @param location 用于去进行要去进行增强的debug类的类名的计算
 */
open class BistouryDebuggerClassFileTransformer(
    private val classFileBuffer: ClassFileBuffer,
    private val sourceJavaFile: String,
    location: ResolvedSourceLocation
) : BaseClassFileTransformer() {

    /**
     * 计算得到debugClassName, 也就是我们要去进行字节码的干预, 去进行动态断点的实现的目标类
     */
    private val debugClassName = Type.getType(location.classSignature).internalName

    @Nullable
    override fun transform(
        @Nullable loader: ClassLoader?,
        className: String,
        classBeingRedefined: Class<*>,
        @Nullable protectionDomain: ProtectionDomain?,
        classfileBuffer: ByteArray
    ): ByteArray? {
        // 如果不是debug的目标类的话, 那么我们不去进行干预, 直接return...
        if (debugClassName != className) {
            return null
        }
        this.classFileBuffer.getLock().lock()
        try {
            // 使用之前已经存在的字节码, 去构建出来ClassReader去进行字节码的读取
            val classReader = ClassReader(this.classFileBuffer.getClassBuffer(classBeingRedefined, classfileBuffer))

            // 利用MetadataCollector, 基于ASM去收集类的相关元信息...
            val classMetadata = ClassMetadata(debugClassName)
            classReader.accept(BistouryClassMetadataCollector(classMetadata), ClassReader.SKIP_FRAMES)

            // 使用ClassWriter去进行Debug类的增强字节码的生成
            val classWriter = ClassWriter(computeFlag(classReader))
            val bistouryDebuggerClassVisitor =
                BistouryDebuggerClassVisitor(CheckClassAdapter(classWriter), sourceJavaFile, classMetadata)
            classReader.accept(bistouryDebuggerClassVisitor, ClassReader.SKIP_FRAMES)

            // 获取到转换完成的字节码, 并将已经转换完成的字节码存起来
            val classByteBuffer = classWriter.toByteArray()
            this.classFileBuffer.setClassBuffer(classBeingRedefined, classByteBuffer)

            return classByteBuffer
        } catch (ex: Exception) {
            ex.printStackTrace()
            return classfileBuffer
        } finally {
            this.classFileBuffer.getLock().unlock()
        }
    }
}