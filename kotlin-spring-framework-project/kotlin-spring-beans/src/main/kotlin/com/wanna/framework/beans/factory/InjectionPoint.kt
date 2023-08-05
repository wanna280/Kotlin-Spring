package com.wanna.framework.beans.factory

import com.wanna.framework.core.MethodParameter
import java.lang.reflect.Field

/**
 * 这是一个依赖的注入点, 它的常见实现是DependencyDescriptor
 */
open class InjectionPoint(private val field: Field? = null, private val parameter: MethodParameter? = null) {
    /**
     * 获取方法参数/构造器参数/字段上的注解列表
     */
    open fun getAnnotations(): Array<Annotation> {
        if (field != null) {
            return field.annotations
        }
        if (parameter != null) {
            return parameter.getAnnotations()
        }
        return emptyArray()
    }

    /**
     * 获取指定的注解, 如果是一个方法参数, 那么从方法参数当中获取注解; 如果是一个字段, 从字段当中获取注解
     */
    open fun <T : Annotation> getAnnotation(annotationClass: Class<T>): T? {
        if (field != null) {
            return field.getAnnotation(annotationClass)
        }
        if (parameter != null) {
            return parameter.getAnnotation(annotationClass)
        }
        return null
    }

    open fun getField(): Field? = this.field


    /**
     * 获取方法参数, 如果这描述的是一个字段, 那么return null
     */
    open fun getMethodParameter(): MethodParameter? = this.parameter
}