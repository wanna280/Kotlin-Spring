package com.wanna.debugger.bistoury.instrument.client.monitor

import com.wanna.debugger.bistoury.instrument.client.common.BaseClassFileTransformer
import com.wanna.debugger.bistoury.instrument.client.common.ClassFileBuffer
import com.wanna.debugger.bistoury.instrument.client.location.ResolvedSourceLocation
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Type
import org.objectweb.asm.util.CheckClassAdapter
import java.lang.instrument.ClassFileTransformer
import java.security.ProtectionDomain
import javax.annotation.Nullable

/**
 * 为动态监控进行增强提供实现的[ClassFileTransformer], 对要去进行添加动态监控的类去进行字节码的生成
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2023/1/31
 */
open class BistouryMonitorClassFileTransformer(
    private val classFileBuffer: ClassFileBuffer,
    private val sourceJavaFile: String,
    private val lineNumber: Int,
    location: ResolvedSourceLocation
) : BaseClassFileTransformer() {

    /**
     * 计算得到monitorClassName, 也就是我们要去进行字节码的干预, 从而实现动态监控的目标类
     */
    private val monitorClassName = Type.getType(location.classSignature).internalName

    /**
     * 要去添加监控的方法名, 与methodDescriptor一起去唯一决定一个方法
     */
    private val methodName: String = location.methodName

    /**
     * 要去添加监控的方法的参数&返回值的描述符, 与methodName一起去唯一决定一个方法
     */
    private val methodDescriptor: String = location.methodDescriptor

    /**
     * 执行对于目标类的转换
     */
    override fun transform(
        @Nullable loader: ClassLoader?,
        className: String,
        classBeingRedefined: Class<*>,
        @Nullable protectionDomain: ProtectionDomain?,
        classfileBuffer: ByteArray
    ): ByteArray? {
        // 如果不是我们想要去进行干预的类, pass
        if (monitorClassName != className) {
            return null
        }
        this.classFileBuffer.getLock().lock()
        try {
            val classReader = ClassReader(this.classFileBuffer.getClassBuffer(classBeingRedefined, classfileBuffer))
            val classWriter = ClassWriter(computeFlag(classReader))

            // 创建一个对动态监控去提供字节码的增强的ClassVisitor, 进行字节码的生成
            val classVisitor = BistouryMonitorClassVisitor(CheckClassAdapter(classWriter), methodName, methodDescriptor)
            classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
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