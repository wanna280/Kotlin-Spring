package com.wanna.framework.core.type

import com.wanna.framework.context.AnnotationAttributesUtils
import com.wanna.framework.util.ClassUtils
import org.springframework.core.annotation.AnnotatedElementUtils

/**
 * 这是一个标准的的AnnotationMetadata
 */
class StandardAnnotationMetadata(val clazz: Class<*>) : AnnotationMetadata {

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
}