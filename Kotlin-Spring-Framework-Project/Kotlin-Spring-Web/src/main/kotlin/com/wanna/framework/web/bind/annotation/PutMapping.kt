package com.wanna.framework.web.bind.annotation

import org.springframework.core.annotation.AliasFor

/**
 * 标识这是一个支持PUT请求的RequestMapping
 */
@RequestMapping(method = [RequestMethod.PUT])
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class PutMapping(
    @get:AliasFor("path", annotation = RequestMapping::class)
    val value: Array<String> = [],
    @get:AliasFor("value", annotation = RequestMapping::class)
    val path: Array<String> = [],
    @get:AliasFor("params", annotation = RequestMapping::class)
    val params: Array<String> = []
)

