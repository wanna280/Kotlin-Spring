package com.wanna.framework.lang

import javax.annotation.Nonnull
import javax.annotation.meta.When

/**
 * 标识这个参数/字段以及方法的返回值可能为null
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.FIELD,
    
    AnnotationTarget.VALUE_PARAMETER,
    AnnotationTarget.PROPERTY
)
@Nonnull(`when` = When.MAYBE)
annotation class Nullable
