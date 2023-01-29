package com.wanna.framework.web.bind.annotation

/**
 * 获取请求路径变量当中的值
 *
 * @param value 需要获取的路径变量的参数名
 */
@Target( AnnotationTarget.VALUE_PARAMETER)
annotation class PathVariable(val value: String = "")
