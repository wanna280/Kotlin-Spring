package com.wanna.framework.web.method.annotation

import com.wanna.framework.web.bind.RequestMethod
import org.springframework.core.annotation.AliasFor

/**
 * RequestMapping
 */
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class RequestMapping(
    @get:AliasFor("path")
    val value: Array<String> = [],
    @get:AliasFor("value")
    val path: Array<String> = [],
    val method: Array<RequestMethod> = [],
    val params: Array<String> = []
)
