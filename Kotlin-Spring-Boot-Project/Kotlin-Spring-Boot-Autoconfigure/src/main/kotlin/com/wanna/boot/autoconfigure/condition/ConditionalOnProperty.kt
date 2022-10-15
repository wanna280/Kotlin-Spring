package com.wanna.boot.autoconfigure.condition

import com.wanna.framework.context.annotation.Conditional

@Target(AnnotationTarget.TYPE, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Conditional([OnPropertyCondition::class])
annotation class ConditionalOnProperty(
    val value: Array<String> = [],
    val name: Array<String> = [],
    val prefix: String = "",
    val havingValue: String = "",
    val matchIfMissing: Boolean = false
)
