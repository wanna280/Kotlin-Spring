package com.wanna.framework.web.bind.annotation

/**
 * 获取请求路径变量当中的值
 */
@Target(AnnotationTarget.TYPE_PARAMETER,AnnotationTarget.VALUE_PARAMETER)
annotation class PathVariable(val value: String = "")
