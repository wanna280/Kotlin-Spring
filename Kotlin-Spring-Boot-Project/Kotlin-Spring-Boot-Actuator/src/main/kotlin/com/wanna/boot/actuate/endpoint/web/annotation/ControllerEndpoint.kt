package com.wanna.boot.actuate.endpoint.web.annotation

import com.wanna.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.core.annotation.AliasFor

@Endpoint
@Target(AnnotationTarget.CLASS)
annotation class ControllerEndpoint(
    @get:AliasFor(value = "id", annotation = Endpoint::class)
    val id: String = ""
)
