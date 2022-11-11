package com.wanna.boot.actuate.endpoint.annotation

/**
 * 标识这是一个SpringBootActuator要去进行暴露的一个Endpoint，支持去进行暴露给用户去进行查看；
 *
 * @param id endpointId
 */
@Target(AnnotationTarget.CLASS)
annotation class Endpoint(val id: String = "")
