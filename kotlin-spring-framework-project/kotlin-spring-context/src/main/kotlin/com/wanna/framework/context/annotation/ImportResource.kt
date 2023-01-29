package com.wanna.framework.context.annotation

import com.wanna.framework.beans.factory.support.BeanDefinitionReader
import com.wanna.framework.core.annotation.AliasFor
import kotlin.reflect.KClass

@Repeatable
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.TYPE, AnnotationTarget.CLASS)
annotation class ImportResource(
    @get:AliasFor("locations")
    val value: Array<String> = [],
    @get:AliasFor("value")
    val locations: Array<String> = [],
    val reader: KClass<out BeanDefinitionReader> = BeanDefinitionReader::class
)
