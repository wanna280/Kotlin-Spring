package com.wanna.framework.web.bind.annotation

import org.springframework.core.annotation.AliasFor

/**
 * 标识这是一个支持POST请求的RequestMapping
 *
 * @see RequestMapping
 */
@RequestMapping(method = [RequestMethod.POST])
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class PostMapping(
    @get:AliasFor("path", annotation = RequestMapping::class)
    val value: Array<String> = [],
    @get:AliasFor("value", annotation = RequestMapping::class)
    val path: Array<String> = [],
    @get:AliasFor("params", annotation = RequestMapping::class)
    val params: Array<String> = [],
    @get:AliasFor("header", annotation = RequestMapping::class)
    val header: Array<String> = [],
    @get:AliasFor("produces", annotation = RequestMapping::class)
    val produces: Array<String> = []
)
