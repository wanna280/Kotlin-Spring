package com.wanna.framework.web.bind.annotation

/**
 * 标识该参数，需要从RequestBody当中去进行获取，可以使用Map/JavaBean去进行接收
 *
 * @param required 是否为必要的？如果为必要的，但是没有给出，会抛出异常
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE_PARAMETER)
annotation class RequestBody(
    val required: Boolean = true
)
