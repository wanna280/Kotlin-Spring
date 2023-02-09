package com.wanna.framework.lang

import javax.annotation.Nonnull
import javax.annotation.meta.When

/**
 * 声明标注当前注解的元素在某些情况下可能为null,
 * 利用JSR-305注解, 向支持JSR-305支持的通用工具去指示Java对象的可空性,
 * Kotlin使用该注解去推断SpringAPI的可空性, 应在方法参数/方法返回值/字段上去进行标注
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.FIELD,
    AnnotationTarget.PROPERTY
)
@Nonnull(`when` = When.MAYBE)
annotation class Nullable
