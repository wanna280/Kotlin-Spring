package com.wanna.framework.web.bind.annotation

@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE_PARAMETER)
annotation class RequestBody(
    val required: Boolean = true
)
