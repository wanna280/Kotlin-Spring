package com.wanna.framework.context.annotations

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CONSTRUCTOR, AnnotationTarget.FIELD)
annotation class Value(val value: String)
