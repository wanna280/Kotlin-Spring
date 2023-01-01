package com.wanna.framework.core.annotation

import kotlin.reflect.KClass

/**
 * 别名注解
 *
 * @param value 别名(同attribute, 不能一起配置, Note: 当value/attribute都为空时, 将会采用标注@AliasFor注解的这个属性的name)
 * @param attribute 别名(同value, 不能一起配置, Note: 当value/attribute都为空时, 将会采用标注@AliasFor注解的这个属性的name)
 * @param annotation 是哪个注解的别名?(默认为标注@AliasFor注解的属性所在的当前注解)
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class AliasFor(
    @get:AliasFor(value = "attribute") val value: String = "",
    @get:AliasFor(value = "value") val attribute: String = "",
    val annotation: KClass<out Annotation> = Annotation::class
)
