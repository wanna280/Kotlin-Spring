package com.wanna.framework.core.type.classreading

import com.wanna.framework.core.annotation.MergedAnnotations
import com.wanna.framework.core.type.AnnotationMetadata
import com.wanna.framework.core.type.MethodMetadata
import com.wanna.framework.lang.Nullable
import com.wanna.framework.util.ClassUtils
import org.objectweb.asm.Opcodes

/**
 * AnnotationMetadata的简单实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/19
 */
open class SimpleAnnotationMetadata(
    private val className: String,
    private val access: Int,
    private val enclosingClassName: String?,
    private val superClassName: String?,
    private val independentInnerClass: Boolean,
    private val interfaceNames: Array<String>,
    private val memberClassNames: Array<String>,
    private val annotatedMethods: Set<MethodMetadata>,
    private val annotations: MergedAnnotations
) : AnnotationMetadata {
    override fun getAnnotations() = this.annotations

    override fun getClassName(): String = this.className

    override fun getPackageName(): String = ClassUtils.getPackageName(className)

    override fun isInterface(): Boolean = (access and Opcodes.ACC_INTERFACE) != 0

    override fun isAnnotation(): Boolean = (access and Opcodes.ACC_ANNOTATION) != 0

    override fun isAbstract(): Boolean = (access and Opcodes.ACC_ABSTRACT) != 0

    override fun isConcrete(): Boolean = !isAbstract()

    override fun isFinal(): Boolean = (access and Opcodes.ACC_FINAL) != 0

    override fun hasEnclosingClass(): Boolean = enclosingClassName != null

    override fun isIndependentInnerClass(): Boolean = this.independentInnerClass

    @Nullable
    override fun getEnclosingClassName(): String? = this.enclosingClassName

    override fun hasSuperClass(): Boolean = this.superClassName != null

    @Nullable
    override fun getSuperClassName(): String? = this.superClassName

    override fun getInterfaceNames(): Array<String> = this.interfaceNames

    override fun getMemberClassNames(): Array<String> = this.memberClassNames

    /**
     * 检查当前类上是否标注了给定的AnnotationName对应的注解?
     *
     * @param annotationName 注解类名
     * @return 如果存在有该注解的话, return null; 否则return true
     */
    override fun isAnnotated(annotationName: String): Boolean {
        return !annotationName.startsWith("java.lang.annotation") && getAnnotations().isPresent(annotationName)
    }

    /**
     * 获取当前类当中, 所有标注了给定的AnnotationName的注解的方法
     *
     * @param annotationName 注解类名
     * @return 标注了该注解的所有的方法的元信息
     */
    override fun getAnnotatedMethods(annotationName: String): Set<MethodMetadata> {
        return annotatedMethods.filter { it.isAnnotated(annotationName) }.toSet()
    }

    override fun toString(): String = this.className
}