package com.wanna.boot.autoconfigure.condition

import com.wanna.framework.context.annotation.Conditional

/**
 * 只要在指定的class都缺少的情况下，才会将该Bean去完成装配到容器当中
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Conditional([OnClassCondition::class])
annotation class ConditionalOnMissingClass(
    val value: Array<String> = []
)
