package com.wanna.boot.autoconfigure.condition

import com.wanna.framework.context.annotation.Conditional
import kotlin.reflect.KClass

/**
 * 只要在给定的class都满足的情况下，才会将该Bean给导入到容器当中
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Conditional([OnClassCondition::class])
annotation class ConditionOnClass(
    val value: Array<KClass<*>> = [],
    val name: Array<String> = []
)
