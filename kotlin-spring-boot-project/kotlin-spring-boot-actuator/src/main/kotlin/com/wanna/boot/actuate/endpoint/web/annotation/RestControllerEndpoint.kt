package com.wanna.boot.actuate.endpoint.web.annotation

import com.wanna.framework.core.annotation.AliasFor
import com.wanna.framework.web.bind.annotation.ResponseBody

@Target(AnnotationTarget.CLASS)
@ControllerEndpoint
@ResponseBody
annotation class RestControllerEndpoint(
    @get:AliasFor("id", annotation = ControllerEndpoint::class)
    val id: String = ""
)
