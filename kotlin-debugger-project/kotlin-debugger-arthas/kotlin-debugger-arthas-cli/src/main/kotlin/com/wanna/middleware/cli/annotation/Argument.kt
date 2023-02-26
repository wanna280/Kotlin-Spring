package com.wanna.middleware.cli.annotation

@Target(AnnotationTarget.FUNCTION)
annotation class Argument(val argName: String = "value", val index: Int, val required: Boolean = true)
