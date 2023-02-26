package com.wanna.middleware.cli.annotation

@Target(AnnotationTarget.FUNCTION)
annotation class Option(
    val argName: String = "value",
    val longName: String = "",
    val shortName: String = "",
    val required: Boolean = false
)
