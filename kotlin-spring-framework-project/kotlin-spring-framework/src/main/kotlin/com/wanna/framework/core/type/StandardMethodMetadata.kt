package com.wanna.framework.core.type

import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.core.annotation.MergedAnnotations
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * 这是一个对于MethodMetadata的标准实现，描述的是一个方法的相关元信息
 *
 * @see AnnotatedTypeMetadata
 */
open class StandardMethodMetadata(private val method: Method) : MethodMetadata {

    /**
     * MergedAnnotations
     */
    private val annotations = MergedAnnotations.from(method)

    override fun getAnnotations() = annotations

    open fun getMethod(): Method = this.method

    override fun isAnnotated(annotationName: String): Boolean {
        return AnnotatedElementUtils.isAnnotated(method, annotationName)
    }

    override fun getMethodName(): String = method.name

    override fun getDeclaringClassName(): String = method.declaringClass.name

    override fun getReturnTypeName(): String = method.returnType.name

    override fun isAbstract() = Modifier.isAbstract(method.modifiers)

    override fun isStatic() = Modifier.isStatic(method.modifiers)

    override fun isOverridable() = !isStatic() && !isFinal() && !isPrivate()

    override fun isFinal() = Modifier.isFinal(method.modifiers)

    override fun isPrivate() = Modifier.isPrivate(method.modifiers)

    override fun toString(): String {
        return method.toString()
    }
}