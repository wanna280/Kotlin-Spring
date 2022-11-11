package com.wanna.boot.actuate.endpoint.web.annotation

import com.wanna.framework.web.bind.annotation.ResponseBody
import org.springframework.core.annotation.AliasFor

@Target(AnnotationTarget.CLASS)
@ControllerEndpoint
@ResponseBody
annotation class RestControllerEndpoint(
    @get:AliasFor("id",annotation = ControllerEndpoint::class)
    val id: String = ""
)
