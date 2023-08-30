package com.wanna.framework.beans.factory

import com.wanna.framework.core.MethodParameter
import com.wanna.framework.lang.Nullable
import java.lang.reflect.Field

/**
 * 这是一个依赖的注入点, 它的常见实现是DependencyDescriptor
 *
 * @param field 字段
 * @param parameter 方法参数
 */
open class InjectionPoint(
    private val field: Field? = null,
    private val parameter: MethodParameter? = null
) {
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
     *
     * @param annotationClass 要去进行寻找的注解类型
     * @return 寻找到的注解, 如果没有合适的, 那么return null
     */
    @Nullable
    open fun <T : Annotation> getAnnotation(annotationClass: Class<T>): T? {
        if (field != null) {
            return field.getAnnotation(annotationClass)
        }
        if (parameter != null) {
            return parameter.getAnnotation(annotationClass)
        }
        return null
    }

    /**
     * 获取到字段, 如果描述的是一个方法参数, 那么return null
     *
     * @return Field
     */
    @Nullable
    open fun getField(): Field? = this.field

    /**
     * 获取方法参数, 如果这描述的是一个字段, 那么return null
     *
     * @return MethodParameter
     */
    @Nullable
    open fun getMethodParameter(): MethodParameter? = this.parameter

    /**
     * 获取方法参数MethodParameter
     *
     * @return MethodParameter
     */
    open fun obtainMethodParameter(): MethodParameter {
        return this.parameter ?: throw IllegalStateException("MethodParameter cannot be null")
    }
}