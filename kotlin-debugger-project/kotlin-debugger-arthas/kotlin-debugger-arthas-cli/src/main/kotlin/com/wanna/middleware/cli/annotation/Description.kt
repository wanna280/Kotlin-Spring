package com.wanna.middleware.cli.annotation

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class Description(val value: String)
