package com.wanna.middleware.cli.annotation

import com.wanna.middleware.cli.converter.Converter
import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class ConvertedBy(val value: KClass<out Converter<*>>)
