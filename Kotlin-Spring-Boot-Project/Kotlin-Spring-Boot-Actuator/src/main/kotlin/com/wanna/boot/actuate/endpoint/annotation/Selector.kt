package com.wanna.boot.actuate.endpoint.annotation

/**
 * 标识这个参数需要作为路径变量，至于具体的路径变量的name，根据参数名去进行决定
 *
 * @see ReadOperation
 * @see WriteOperation
 * @see DeleteOperation
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE_PARAMETER)
annotation class Selector