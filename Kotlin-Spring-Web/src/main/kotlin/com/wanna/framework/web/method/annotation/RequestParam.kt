package com.wanna.framework.web.method.annotation

import org.springframework.core.annotation.AliasFor

/**
 * 标注当前方法参数，需要获取请求参数当中的值，它会被RequestParamMethodArgumentResolver处理器所进行解析；
 * 可以通过name/value属性去配置，要获取的headerName；如果没有配置name/value，将会采用方法的参数名作为headerName去进行寻找
 *
 * @see RequestParamMethodArgumentResolver
 */
@Target(AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.VALUE_PARAMETER)
annotation class RequestParam(
    /**
     * 参数名，同value
     */
    @get:AliasFor("value")
    val name: String = "",

    /**
     * 参数名，同name
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
