package com.wanna.framework.web.bind.annotation

import kotlin.reflect.KClass

/**
 * 标识这是一个处理支持去处理请求当中的异常的ExceptionHandler，
 *
 * * 1.可以在Controller类当中去进行局部的ExceptionHandler的配置，
 * * 2.也可以在ControllerAdvice当中去进行全局的ExceptionHandler的配置
 *
 * @param value 支持去进行处理的异常的类型
 * @see ControllerAdvice
 * @see RestController
 * @see RequestMapping
 */
@Target(AnnotationTarget.FUNCTION)
annotation class ExceptionHandler(val value:Array<KClass<out Throwable>> = [])
