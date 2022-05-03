package com.wanna.framework.core.type

import com.wanna.framework.context.annotation.AnnotationAttributesUtils
import com.wanna.framework.core.util.ClassUtils
import org.springframework.core.annotation.AnnotatedElementUtils

/**
 * 这是一个标准的的AnnotationMetadata
 */
open class StandardAnnotationMetadata(val clazz: Class<*>) : AnnotationMetadata {

    override fun getAnnotations(): Array<Annotation> {
        return clazz.annotations
    }

    override fun getAnnotationAttributes(annotationName: String): Map<String, Any> {
        return getAnnotationAttributes(ClassUtils.forName(annotationName))
    }

    override fun getAnnotationAttributes(annotationClass: Class<out Annotation>): Map<String, Any> {
        val mergedAnnotation = AnnotatedElementUtils.getMergedAnnotation(clazz, annotationClass)
        return AnnotationAttributesUtils.asAnnotationAttributes(mergedAnnotation) ?: emptyMap()
    }

    override fun isAnnotated(annotationName: String): Boolean {
        return AnnotatedElementUtils.isAnnotated(clazz, annotationName)
    }

    override fun getClassName(): String {
        return clazz.name
    }
}