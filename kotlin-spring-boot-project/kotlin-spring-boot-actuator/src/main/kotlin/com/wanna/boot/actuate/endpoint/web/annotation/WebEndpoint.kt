package com.wanna.boot.actuate.endpoint.web.annotation

import com.wanna.boot.actuate.endpoint.annotation.Endpoint
import com.wanna.framework.core.annotation.AliasFor

/**
 * Web环境下的Endpoint
 */
@Endpoint
@Target(AnnotationTarget.CLASS)
annotation class WebEndpoint(
    @get:AliasFor(annotation = Endpoint::class, value = "id")
    val id: String
)
