package com.wanna.framework.web.method.annotation

import com.wanna.framework.beans.TypeMismatchException
import com.wanna.framework.core.MethodParameter

/**
 * 方法参数类型不匹配的异常, 在SpringMVC当中针对一个Controller的方法的参数的类型不匹配的情况
 *
 * @author jianchao.jia
 * @version v1.0
 * @date 2022/10/20
 *
 * @param value 原始的待转换的对象
 * @param requiredType requiredType
 * @param name 参数名
 * @param param 参数对象
 * @param cause errorCause
 */
open class MethodArgumentTypeMismatchException(
    value: Any,
    requiredType: Class<*>,
    val name: String,
    val param: MethodParameter,
    cause: Throwable?
) : TypeMismatchException(value, requiredType, cause)