package com.wanna.middleware.cli.annotation

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Argument(val argName: String = "value", val index: Int, val required: Boolean = true)
