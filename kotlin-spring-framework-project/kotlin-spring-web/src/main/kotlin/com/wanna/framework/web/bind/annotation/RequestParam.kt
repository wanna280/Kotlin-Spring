package com.wanna.framework.web.bind.annotation

import com.wanna.framework.web.method.annotation.RequestParamMethodArgumentResolver
import org.springframework.core.annotation.AliasFor

/**
 * 标注当前方法参数，需要获取请求参数当中的值，它会被RequestParamMethodArgumentResolver处理器所进行解析；
 * 可以通过name/value属性去配置，要获取的headerName；如果没有配置name/value，将会采用方法的参数名作为headerName去进行寻找
 *
 * @see RequestParamMethodArgumentResolver
 * @param name header名，同value属性
 * @param value header名，同name属性
 * @param required 该请求参数是否是必须的?
 * @param defaultValue 如果请求当中不存在该header的参数, 将要使用什么作为默认值
 */
@Target(AnnotationTarget.TYPE_PARAMETER, AnnotationTarget.VALUE_PARAMETER)
annotation class RequestParam(
    @get:com.wanna.framework.core.annotation.AliasFor("value")
    @get:AliasFor("value")
    val name: String = "",
    @get:AliasFor("name")
    @get:com.wanna.framework.core.annotation.AliasFor("name")
    val value: String = "",
    val required: Boolean = true,
    val defaultValue: String = ValueConstants.DEFAULT_NONE
)
