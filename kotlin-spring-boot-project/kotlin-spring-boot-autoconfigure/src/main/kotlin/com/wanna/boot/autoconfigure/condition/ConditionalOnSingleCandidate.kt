package com.wanna.boot.autoconfigure.condition

import com.wanna.framework.context.annotation.Conditional
import kotlin.reflect.KClass

/**
 * 只有在容器中只要一个指定的候选Bean时, 才去对Bean去进行装配
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
@Conditional(OnBeanCondition::class)
annotation class ConditionalOnSingleCandidate(
    val value: KClass<*> = Any::class,
    val type: String = ""
)
