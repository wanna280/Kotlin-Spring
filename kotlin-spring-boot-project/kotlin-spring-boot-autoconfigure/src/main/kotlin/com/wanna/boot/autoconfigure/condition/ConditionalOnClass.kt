package com.wanna.boot.autoconfigure.condition

import com.wanna.framework.context.annotation.Conditional
import kotlin.reflect.KClass

/**
 * 只要在给定的所有Class都在当前的JVM当中的情况下，才会将该Bean给导入到SpringBeanFactory当中
 *
 * @param value 要去进行匹配的类, 以Class的方式去进行给出
 * @param name 要去进行匹配的类, 以ClassName的方式去进行给出
 */
@Target(AnnotationTarget.TYPE, AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Conditional(OnClassCondition::class)
annotation class ConditionalOnClass(
    val value: Array<KClass<*>> = [], val name: Array<String> = []
)
