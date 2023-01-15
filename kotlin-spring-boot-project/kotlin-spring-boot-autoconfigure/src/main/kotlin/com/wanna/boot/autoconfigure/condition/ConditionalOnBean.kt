package com.wanna.boot.autoconfigure.condition

import com.wanna.framework.context.annotation.Conditional
import kotlin.reflect.KClass

/**
 * 只要在给定的Bean都满足时, 才将标注该注解的Bean导入到容器当中
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS,AnnotationTarget.FUNCTION)
@Conditional(OnBeanCondition::class)
annotation class ConditionalOnBean(
    val value: Array<KClass<*>> = [],
    val type: Array<String> = [],
    val annotation: Array<KClass<out Annotation>> = [],
    val name: Array<String> = []
)
