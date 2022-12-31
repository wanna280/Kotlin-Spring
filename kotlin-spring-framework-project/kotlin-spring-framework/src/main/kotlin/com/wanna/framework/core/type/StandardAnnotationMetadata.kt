package com.wanna.framework.core.type

import com.wanna.framework.context.annotation.AnnotationAttributesUtils
import com.wanna.framework.core.annotation.AnnotatedElementUtils
import com.wanna.framework.core.annotation.MergedAnnotations
import com.wanna.framework.util.ClassUtils
import com.wanna.framework.util.ReflectionUtils
import java.lang.reflect.Modifier

/**
 * 这是一个标准的的AnnotationMetadata的实现
 *
 * @param introspectedClass 要去描述的目标类
 */
open class StandardAnnotationMetadata(val introspectedClass: Class<*>) : AnnotationMetadata {

    /**
     * MergedAnnotations
     */
    private val annotations = MergedAnnotations.from(introspectedClass)

    override fun getAnnotations() = this.annotations

    override fun getAnnotationAttributes(annotationName: String): Map<String, Any> {
        return getAnnotationAttributes(ClassUtils.forName(annotationName))
    }

    override fun getAnnotationAttributes(annotationClass: Class<out Annotation>): Map<String, Any> {
        val mergedAnnotation =
            AnnotatedElementUtils.getMergedAnnotation(introspectedClass, annotationClass) ?: return emptyMap()
        return AnnotationAttributesUtils.asAnnotationAttributes(mergedAnnotation) ?: emptyMap()
    }

    override fun isAnnotated(annotationName: String): Boolean {
        val annotationClass = ClassUtils.getAnnotationClassFromString<Annotation>(annotationName)
        return AnnotatedElementUtils.getMergedAnnotation(introspectedClass, annotationClass) != null
    }

    override fun getAnnotatedMethods(annotationName: String): Set<MethodMetadata> {
        val methodMetadatas = LinkedHashSet<MethodMetadata>()
        ReflectionUtils.doWithLocalMethods(introspectedClass) {
            it.annotations.forEach { ann ->
                if (ann.annotationClass.java.name == annotationName) {
                    methodMetadatas += StandardMethodMetadata(it)
                }
            }
        }
        return methodMetadatas
    }

    override fun getClassName(): String = introspectedClass.name

    override fun getPackageName(): String = introspectedClass.packageName

    override fun isInterface(): Boolean = introspectedClass.isInterface

    override fun isAnnotation(): Boolean = introspectedClass.isAnnotation

    override fun isAbstract(): Boolean = Modifier.isAbstract(introspectedClass.modifiers)

    override fun isConcrete(): Boolean = !isConcrete()

    override fun isFinal(): Boolean = Modifier.isFinal(introspectedClass.modifiers)

    override fun hasEnclosingClass(): Boolean = introspectedClass.enclosingClass != null

    override fun getEnclosingClassName(): String? = introspectedClass.enclosingClass.name

    override fun hasSuperClass(): Boolean = introspectedClass.superclass != null

    override fun getSuperClassName(): String? = introspectedClass.superclass.name

    override fun getInterfaceNames(): Array<String> = introspectedClass.interfaces.map { it.name }.toTypedArray()

    override fun getMemberClassNames(): Array<String> = introspectedClass.declaredClasses.map { it.name }.toTypedArray()

    override fun isIndependentInnerClass(): Boolean =
        hasEnclosingClass() && Modifier.isStatic(introspectedClass.modifiers)

    companion object {
        /**
         * 给定一个clazz，去进行构建
         */
        @JvmStatic
        fun from(clazz: Class<*>): StandardAnnotationMetadata {
            return StandardAnnotationMetadata(clazz)
        }
    }
}