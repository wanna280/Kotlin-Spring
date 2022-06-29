package com.wanna.boot.actuate.endpoint.annotation

@Target(AnnotationTarget.CLASS)
annotation class Endpoint(val id: String = "")
