package com.wanna.framework.context.annotation

import org.springframework.core.annotation.AliasFor
import kotlin.reflect.KClass

@Repeatable
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.TYPE, AnnotationTarget.CLASS)
annotation class ImportResource(
    @get:com.wanna.framework.core.annotation.AliasFor("locations")
    @get:AliasFor("locations")
    val value: Array<String> = [],
    @get:com.wanna.framework.core.annotation.AliasFor("value")
    @get:AliasFor("value")
    val locations: Array<String> = [],
    val reader: KClass<out BeanDefinitionReader> = BeanDefinitionReader::class
)
