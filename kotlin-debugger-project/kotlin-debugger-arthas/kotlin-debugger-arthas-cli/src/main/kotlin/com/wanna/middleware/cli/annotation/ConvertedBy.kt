package com.wanna.middleware.cli.annotation

import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class ConvertedBy(val value: KClass<*>)
