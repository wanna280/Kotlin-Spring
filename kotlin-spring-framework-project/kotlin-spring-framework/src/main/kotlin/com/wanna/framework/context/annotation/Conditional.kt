package com.wanna.framework.context.annotation

import kotlin.reflect.KClass

/**
 * 使用Conditional标注的Bean，需要符合条件时，才会被装配到容器当中；
 * 通过value当中配置对应的Condition对象去完成匹配的条件
 *
 * @see Condition
 * @see ConditionContext
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class Conditional(
    vararg val value: KClass<out Condition> = []
)
