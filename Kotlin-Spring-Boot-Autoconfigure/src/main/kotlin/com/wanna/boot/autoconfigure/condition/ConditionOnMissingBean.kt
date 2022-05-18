package com.wanna.boot.autoconfigure.condition

import com.wanna.framework.context.annotation.Conditional
import kotlin.reflect.KClass

/**
 * 只要当给定的Bean都不存在于容器当中时，才会完成装配
 *
 * @see OnBeanCondition
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS,AnnotationTarget.FUNCTION)
@Conditional([OnBeanCondition::class])
annotation class ConditionOnMissingBean(
    val value: Array<KClass<*>> = [],
    val type: Array<String> = [],
    val annotation: Array<KClass<out Annotation>> = [],
    val name: Array<String> = []
)
