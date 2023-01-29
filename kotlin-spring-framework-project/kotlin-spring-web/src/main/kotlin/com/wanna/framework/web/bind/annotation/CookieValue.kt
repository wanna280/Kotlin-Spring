package com.wanna.framework.web.bind.annotation

/**
 * 支持标注在一个方法参数上, 用于从request当中去根据cookieName去获取一个CookieValue的注解
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/11/22
 *
 * @param value cookieName, 同name
 * @param name cookieName, 同value
 * @param required 是否必须存在?如果不存在则不支持去处理这个请求
 * @param defaultValue 如果required=false允许不存在的话, 那么默认值是什么?
 *
 * @see com.wanna.framework.web.method.annotation.ServerCookieValueMethodArgumentResolver
 */
@Target( AnnotationTarget.VALUE_PARAMETER)
annotation class CookieValue(
    val value: String = "",
    val name: String = "",
    val required: Boolean = true,
    val defaultValue: String = ValueConstants.DEFAULT_NONE
)