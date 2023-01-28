package com.wanna.framework.web.bind.annotation

import com.wanna.framework.core.annotation.AliasFor
import com.wanna.framework.web.http.MediaType

/**
 * RequestMapping, 负责处理指定的映射下的请求, 并将它交给指定的HandlerMethod去进行处理
 *
 * @param value path, 同path
 * @param path path, 同value
 * @param params 要匹配的参数
 * @param method 支持的请求方式
 * @param headers 要匹配的header(只有全部的headerName都匹配时, 才能匹配到当前的HandlerMethod)
 * @param consumes 该Handler方法支持去处理什么样的MediaType的RequestBody? 针对客户端的"Content-Type"去进行匹配 see [MediaType]
 * @param produces 想要产出的MediaType(想要将数据以怎么样的形式写出给客户端? ) see [MediaType]
 *
 * @see MediaType
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
    val headers: Array<String> = [],
    val consumes: Array<String> = [],
    val produces: Array<String> = []
)
