package com.wanna.framework.core.annotation

import org.springframework.core.annotation.AnnotatedElementUtils
import java.lang.reflect.AnnotatedElement

/**
 * AnnotatedElement的工具类，负责桥接SpringCore包当中的AnnotatedElementUtils
 */
object AnnotatedElementUtils {

    @JvmStatic
    fun isAnnotated(element: AnnotatedElement, annotationType: Class<out Annotation>): Boolean {
        return getAnnotations(element).isPresent(annotationType)
    }

    @JvmStatic
    fun isAnnotated(element: AnnotatedElement, annotationClassName: String): Boolean {
        return getAnnotations(element).isPresent(annotationClassName)
    }

    @JvmStatic
    fun <A : Annotation> getMergedAnnotation(element: AnnotatedElement, annotationType: Class<A>): A? {
        return AnnotatedElementUtils.getMergedAnnotation(element, annotationType)
    }

    @JvmStatic
    fun <A : Annotation> getAllMergedAnnotations(element: AnnotatedElement, annotationType: Class<A>): Set<A> {
        return AnnotatedElementUtils.getAllMergedAnnotations(element, annotationType)
    }

    @JvmStatic
    fun <A : Annotation> findAllMergedAnnotations(element: AnnotatedElement, annotationType: Class<A>): Set<A> {
        return AnnotatedElementUtils.findAllMergedAnnotations(element, annotationType)
    }

    /**
     * 判断目标元素上是否有给定的注解(支持使用继承的方式去进行检查父类)
     *
     * @param element 目标元素(方法/字段等)
     * @param annotationType 要去进行匹配的注解
     * @return 如果存在有目标注解的户，return true；否则return false
     */
    @JvmStatic
    fun hasAnnotation(element: AnnotatedElement, annotationType: Class<out Annotation>): Boolean {
        return AnnotatedElementUtils.hasAnnotation(element, annotationType)
    }

    @JvmStatic
    private fun getAnnotations(annotatedElement: AnnotatedElement): MergedAnnotations {
        return MergedAnnotations.from(annotatedElement, MergedAnnotations.SearchStrategy.INHERITED_ANNOTATIONS, null)
    }

    private fun findAnnotations(annotatedElement: AnnotatedElement): MergedAnnotations {
        return MergedAnnotations.from(annotatedElement, MergedAnnotations.SearchStrategy.TYPE_HIERARCHY, null)
    }
}