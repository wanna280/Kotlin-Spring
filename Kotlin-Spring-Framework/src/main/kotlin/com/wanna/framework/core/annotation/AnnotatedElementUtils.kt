package com.wanna.framework.core.annotation

import org.springframework.core.annotation.AnnotatedElementUtils
import java.lang.reflect.AnnotatedElement

/**
 * AnnotatedElement的工具类，负责桥接SpringCore包当中的AnnotatedElementUtils
 */
object AnnotatedElementUtils {
    fun isAnnotated(element: AnnotatedElement, annotationType: Class<out Annotation>): Boolean {
        return AnnotatedElementUtils.isAnnotated(element, annotationType)
    }

    fun isAnnotated(element: AnnotatedElement, annotationClassName: String): Boolean {
        return AnnotatedElementUtils.isAnnotated(element, annotationClassName)
    }

    fun <A : Annotation> getMergedAnnotation(element: AnnotatedElement, annotationType: Class<A>): A? {
        return AnnotatedElementUtils.getMergedAnnotation(element, annotationType)
    }

    fun <A : Annotation> getAllMergedAnnotations(element: AnnotatedElement, annotationType: Class<A>): Set<A> {
        return AnnotatedElementUtils.getAllMergedAnnotations(element, annotationType)
    }

    fun <A : Annotation> findAllMergedAnnotations(element: AnnotatedElement, annotationType: Class<A>): Set<A> {
        return AnnotatedElementUtils.findAllMergedAnnotations(element, annotationType)
    }

    fun hasAnnotation(element: AnnotatedElement, annotationType: Class<out Annotation>): Boolean {
        return AnnotatedElementUtils.hasAnnotation(element,annotationType)
    }
}