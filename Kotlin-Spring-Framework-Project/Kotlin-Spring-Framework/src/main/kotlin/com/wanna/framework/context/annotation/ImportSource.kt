package com.wanna.framework.context.annotation

import org.springframework.core.annotation.AliasFor
import kotlin.reflect.KClass

@Repeatable
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.TYPE, AnnotationTarget.CLASS)
annotation class ImportSource(
    @get:AliasFor("locations")
    val value: Array<String> = [],
    @get:AliasFor("value")
    val locations: Array<String> = [],

    /**
     * out XXX  <==>  ? extends XXX
     */
    val reader: KClass<out BeanDefinitionReader> = BeanDefinitionReader::class
)
