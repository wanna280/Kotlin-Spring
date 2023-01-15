package com.wanna.boot.actuate.endpoint.annotation

/**
 * 标识这个Endpoint当中的这个方法支持去进行"READ"操作, 
 * 对应的就是HTTP请求的"GET"操作
 *
 * @see DeleteOperation
 * @see WriteOperation
 */
@Target(AnnotationTarget.FUNCTION)
annotation class ReadOperation
