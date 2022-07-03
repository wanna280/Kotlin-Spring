package com.wanna.framework.web.bind.annotation

import org.springframework.core.annotation.AliasFor

/**
 * 标注当前方法参数需要根据headerName获取到requestHeader当中的值来去进行设置，它会被RequestHeaderMethodArgumentResolver所处理
 *
 * @see RequestHeader
 * @see RequestHeaderMethodArgumentResolver
 */
@Target(AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.VALUE_PARAMETER)
annotation class RequestHeader(
    /**
     * header名，同value
     */
    @get:AliasFor("value")
    val name: String = "",

    /**
     * header名，同name
     */
    @get:AliasFor("name")
    val value: String = "",

    /**
     * 是否是必须的？
     */
    val required: Boolean = true,

    /**
     * 默认值？
     */
    val defaultValue: String = ""
)
