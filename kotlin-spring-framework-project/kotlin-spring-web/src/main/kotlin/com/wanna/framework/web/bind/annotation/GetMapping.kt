package com.wanna.framework.web.bind.annotation

import org.springframework.core.annotation.AliasFor

/**
 * 标识这是一个支持GET请求的RequestMapping
 *
 * @see RequestMapping
 */
@RequestMapping(method = [RequestMethod.GET])
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.CLASS, AnnotationTarget.TYPE)
annotation class GetMapping(
    @get:com.wanna.framework.core.annotation.AliasFor("path", annotation = RequestMapping::class)
    @get:AliasFor("path", annotation = RequestMapping::class)
    val value: Array<String> = [],
    @get:com.wanna.framework.core.annotation.AliasFor("value", annotation = RequestMapping::class)
    @get:AliasFor("value", annotation = RequestMapping::class)
    val path: Array<String> = [],
    @get:com.wanna.framework.core.annotation.AliasFor("params", annotation = RequestMapping::class)
    @get:AliasFor("params", annotation = RequestMapping::class)
    val params: Array<String> = [],
    @get:com.wanna.framework.core.annotation.AliasFor("header", annotation = RequestMapping::class)
    @get:AliasFor("header", annotation = RequestMapping::class)
    val header: Array<String> = [],
    @get:com.wanna.framework.core.annotation.AliasFor("produces", annotation = RequestMapping::class)
    @get:AliasFor("produces", annotation = RequestMapping::class)
    val produces: Array<String> = []
)
