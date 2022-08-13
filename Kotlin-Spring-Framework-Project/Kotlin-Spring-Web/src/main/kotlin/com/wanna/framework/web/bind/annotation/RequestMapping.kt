package com.wanna.framework.web.bind.annotation

import org.springframework.core.annotation.AliasFor

/**
 * RequestMapping，负责处理指定的映射下的请求，并将它交给指定的方法去进行处理
 *
 * @param value path，同path
 * @param path path，同value
 * @param params 要匹配的参数
 * @param method 支持的请求方式
 * @param header 要匹配的header
 * @param produces 想要产出的MediaType
 */
@Mapping
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.CLASS)
annotation class RequestMapping(
    @get:AliasFor("path")
    val value: Array<String> = [],
    @get:AliasFor("value")
    val path: Array<String> = [],
    val method: Array<RequestMethod> = [],
    val params: Array<String> = [],
    val header: Array<String> = [],
    val produces: Array<String> = []
)
