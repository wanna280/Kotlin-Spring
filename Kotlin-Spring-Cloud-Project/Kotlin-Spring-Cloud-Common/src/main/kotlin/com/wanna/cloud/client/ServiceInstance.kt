package com.wanna.cloud.client

/**
 * 这是SpringCloud对于一个微服务的实例去进行的一层抽象
 */
interface ServiceInstance {
    fun getServiceId(): String
    fun getInstanceId(): String? = null
    fun getHost(): String
    fun getPort(): Int
    fun getUri() : String
    fun getMetadata(): Map<String, String>
}