package com.wanna.framework.core.type.classreading

import com.wanna.framework.context.annotation.AnnotationAttributes
import com.wanna.framework.core.annotation.MergedAnnotation
import com.wanna.framework.core.annotation.MergedAnnotations
import com.wanna.framework.core.type.MethodMetadata
import com.wanna.framework.util.MultiValueMap
import org.objectweb.asm.Opcodes

/**
 * MethodMetadata的简单实现
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/12/19
 *
 * @see MethodMetadata
 */
open class SimpleMethodMetadata(
    private val methodName: String,
    private val declaringClassName: String,
    private val returnTypeName: String,
    private val access: Int,
    private val annotations: MergedAnnotations,
    private val attributesMap: MultiValueMap<String, AnnotationAttributes>,
    private val annotationSet: Set<String>
) : MethodMetadata {

    override fun getAnnotations(): Array<Annotation> = emptyArray()

    override fun getMergedAnnotations(): MergedAnnotations = this.annotations

    override fun getAnnotationAttributes(annotationName: String): Map<String, Any> {
        return attributesMap.getFirst(annotationName) ?: emptyMap()
    }

    override fun getAnnotationTypes(): Set<String> = this.annotationSet

    override fun hasAnnotation(annotationName: String): Boolean = attributesMap.containsKey(annotationName)

    override fun isAnnotated(annotationName: String): Boolean =
        !annotationName.startsWith("java.lang.annotation") && attributesMap.containsKey(annotationName)

    override fun getMethodName(): String = this.methodName

    override fun getDeclaringClassName(): String = this.declaringClassName

    override fun getReturnTypeName(): String = this.returnTypeName

    override fun isAbstract(): Boolean = (access and Opcodes.ACC_ABSTRACT) != 0

    override fun isStatic(): Boolean = (access and Opcodes.ACC_STATIC) != 0

    override fun isOverridable(): Boolean = !isFinal() && !isPrivate() && !isStatic()

    override fun isPrivate(): Boolean = (access and Opcodes.ACC_PRIVATE) != 0

    override fun isFinal(): Boolean = (access and Opcodes.ACC_FINAL) != 0
}