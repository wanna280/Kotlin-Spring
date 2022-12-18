package com.wanna.framework.core.type.classreading

import com.wanna.framework.context.annotation.AnnotationAttributes
import com.wanna.framework.core.type.AnnotationMetadata
import com.wanna.framework.core.type.MethodMetadata
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.LinkedMultiValueMap
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type

/**
 * 基于ASM的方式去提供AnnotationMetadata的读取的Visitor
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/18
 *
 * @param classLoader ClassLoader
 */
open class AnnotationMetadataReadingVisitor(protected val classLoader: ClassLoader) : ClassMetadataReadingVisitor() {

    /**
     * 当前的类上标注的的注解类名列表
     */
    protected val annotationSet = LinkedHashSet<String>()

    /**
     * MetaAnnotationMap
     */
    protected val metaAnnotationMap = LinkedHashMap<String, Set<String>>()

    /**
     * 注解属性信息的Map(Key-Annotation ClassName, Value-该注解对应的注解的属性信息)
     */
    protected val attributesMap = LinkedMultiValueMap<String, AnnotationAttributes>()

    /**
     * 一个类当中的各个方法的Metadata的集合
     */
    protected val methodMetadataSet = LinkedHashSet<MethodMetadata>()

    /**
     * AnnotationMetadata, 作为访问完成之后的最终结果的输出
     */
    private var annotationMetadata: SimpleAnnotationMetadata? = null

    /**
     * 访问类当中的方法时, 我们将方法的信息去收集起来
     *
     * @param access 方法的访问标识符
     * @param name 方法名
     * @param descriptor 方法的描述符
     * @param signature 方法签名
     * @param exceptions 方法的异常表(可能为null)
     * @return 提供MethodMetadata的访问的MethodVisitor
     */
    override fun visitMethod(
        access: Int,
        name: String,
        descriptor: String,
        signature: String?,
        @Nullable exceptions: Array<out String>?
    ): MethodVisitor {
        // 桥接方法直接invoke Super
        if ((access and Opcodes.ACC_BRIDGE) != 0) {
            super.visitMethod(access, name, descriptor, signature, exceptions)
        }

        // 提供对于方法的MethodMetadata的访问的Visitor
        return MethodMetadataReadingVisitor(
            name, access, className!!,
            Type.getReturnType(descriptor).className, classLoader, methodMetadataSet
        )
    }

    /**
     * 访问类上的一个注解时, 我们将注解信息去收集起来
     *
     * @param descriptor descriptor
     * @return 获取一个提供注解的访问的AnnotationVisitor
     */
    override fun visitAnnotation(descriptor: String, visible: Boolean): AnnotationVisitor {
        val className = Type.getType(descriptor).className
        this.annotationSet += className
        return AnnotationAttributesReadingVisitor(className, attributesMap, this.metaAnnotationMap, classLoader)
    }

    /**
     * 在访问完成之后, 构建出来AnnotationMetadata
     */
    override fun visitEnd() {
        this.annotationMetadata = SimpleAnnotationMetadata(
            className!!,
            access,
            enclosingClassName,
            superClassName,
            independentInnerClass,
            interfaces ?: emptyArray(),
            memberClassNames.toTypedArray(),
            this.methodMetadataSet,
            emptyArray(),
            attributesMap,
            annotationSet
        )
    }

    open fun getMetadata(): SimpleAnnotationMetadata =
        this.annotationMetadata ?: throw IllegalStateException("AnnotationMetadata is  null")
}