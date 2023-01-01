package com.wanna.framework.core.type.classreading

import com.wanna.framework.core.annotation.MergedAnnotation
import com.wanna.framework.core.annotation.MergedAnnotations
import com.wanna.framework.core.type.MethodMetadata
import com.wanna.framework.lang.Nullable
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

/**
 * 基于ASM的方式去提供AnnotationMetadata的读取的ClassVisitor
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/18
 *
 * @param classLoader ClassLoader
 * @see org.objectweb.asm.ClassVisitor
 */
open class AnnotationMetadataReadingVisitor(protected val classLoader: ClassLoader) : ClassMetadataReadingVisitor() {

    /**
     * 当前的类上标注的的注解类名列表
     */
    protected val annotationSet = LinkedHashSet<String>()

    /**
     * 一个类当中的各个方法的Metadata的集合
     */
    protected val annotatedMethods = LinkedHashSet<MethodMetadata>()

    /**
     * AnnotationMetadata, 作为访问完成之后的最终结果的输出
     */
    private var annotationMetadata: SimpleAnnotationMetadata? = null

    /**
     * MergedAnnotations
     */
    private var mergedAnnotations: MutableList<MergedAnnotation<Annotation>> = ArrayList()

    /**
     * 访问一个类当中的方法时, 我们需要返回一个MethodVisitor, 去将方法的信息去收集起来
     *
     * @param access 方法的访问标识符
     * @param name 方法名
     * @param descriptor 方法的描述符
     * @param signature 方法签名
     * @param exceptions 方法的异常表(可能为null)
     * @return 提供MethodMetadata的访问的MethodVisitor
     */
    override fun visitMethod(
        access: Int, name: String, descriptor: String, signature: String?, @Nullable exceptions: Array<out String>?
    ): MethodVisitor {
        // 桥接方法直接invoke Super
        if ((access and Opcodes.ACC_BRIDGE) != 0) {
            return super.visitMethod(access, name, descriptor, signature, exceptions)
        }

        // 提供对于方法的MethodMetadata的访问的Visitor
        return MethodMetadataReadingVisitor(
            name, access, className!!, Type.getReturnType(descriptor).className, classLoader, annotatedMethods
        )
    }

    /**
     * 访问类上的一个注解时, 我们需要去返回一个AnnotationVisitor将注解信息去收集起来
     *
     * @param descriptor descriptor
     * @return 获取一个提供注解的访问的AnnotationVisitor
     */
    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
        val className = Type.getType(descriptor).className
        this.annotationSet += className
        return MergedAnnotationReadingVisitor.get(classLoader, null, descriptor, this.mergedAnnotations)
    }

    /**
     * 在访问应该类完成之后, 我们需要去构建出来AnnotationMetadata
     */
    override fun visitEnd() {
        val annotations = MergedAnnotations.of(mergedAnnotations.toTypedArray())
        this.annotationMetadata = SimpleAnnotationMetadata(
            className!!, access, enclosingClassName, superClassName,
            independentInnerClass, interfaces ?: emptyArray(),
            memberClassNames.toTypedArray(), this.annotatedMethods, annotations
        )
    }

    /**
     * 获取到AnnotationMetadata
     *
     * @return AnnotationMetadata
     */
    open fun getMetadata(): SimpleAnnotationMetadata =
        this.annotationMetadata ?: throw IllegalStateException("AnnotationMetadata is null")
}