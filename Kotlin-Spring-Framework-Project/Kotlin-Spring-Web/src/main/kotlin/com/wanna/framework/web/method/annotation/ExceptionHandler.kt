package com.wanna.framework.web.method.annotation

import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
annotation class ExceptionHandler(val value:Array<KClass<out Throwable>> = [])
