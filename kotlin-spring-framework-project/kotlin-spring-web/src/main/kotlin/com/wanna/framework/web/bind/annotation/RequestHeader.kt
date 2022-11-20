package com.wanna.framework.web.bind.annotation

import com.wanna.framework.web.method.annotation.RequestHeaderMethodArgumentResolver
import org.springframework.core.annotation.AliasFor

/**
 * 标注当前方法参数需要根据headerName获取到requestHeader当中的值来去进行设置，它会被RequestHeaderMethodArgumentResolver所处理
 *
 * @see RequestHeader
 * @see RequestHeaderMethodArgumentResolver
 *
 * @param name header名，同value属性
 * @param value header名，同name属性
 * @param required 该请求参数是否是必须的?
 * @param defaultValue 如果请求当中不存在该header的参数, 将要使用什么作为默认值
 */
@Target(AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.VALUE_PARAMETER)
annotation class RequestHeader(
    @get:AliasFor("name")
    val value: String = "",
    @get:AliasFor("value")
    val name: String = "",
    val required: Boolean = true,
    val defaultValue: String = ValueConstants.DEFAULT_NONE
)
