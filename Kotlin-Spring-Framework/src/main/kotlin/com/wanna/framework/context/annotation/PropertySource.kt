package com.wanna.framework.context.annotation

import org.springframework.core.annotation.AliasFor
import kotlin.reflect.KClass

@Repeatable
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class PropertySource(
    @get:AliasFor("locations")
    val value: Array<String> = [],
    @get:AliasFor("value")
    val locations: Array<String> = [],
    val factory: KClass<out PropertySourceFactory> = PropertySourceFactory::class
)
