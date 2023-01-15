package com.wanna.framework.web.bind.annotation

import com.wanna.framework.core.annotation.AliasFor

/**
 * 通过这个注解, 可以实现类级别, 或者是方法级别的跨域的配置
 * 标注这个注解在@RequestMapping的方法上, 或者是@Controller的类上标识该接口允许跨域
 *
 * @param value 允许的Origin, 同origins
 * @param origins 允许的Origin, 同value
 * @param allowCredentials
 * @param allowedHeaders 需要匹配的Header
 * @param originPatterns 使用正则表达式的方式去匹配Origin
 * @param exposedHeaders CORS请求当中要暴露给客户端的Header
 * @param maxAge CORS的存活时间("PreFlight"的有效时间)
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class CrossOrigin(
    @get:AliasFor("origins")
    val value: Array<String> = [],
    @get:AliasFor("value")
    val origins: Array<String> = [],
    val allowedHeaders: Array<String> = [],
    val methods: Array<RequestMethod> = [],
    val originPatterns: Array<String> = [],
    val exposedHeaders: Array<String> = [],
    val allowCredentials: String = "",
    val maxAge: Long = -1
)
