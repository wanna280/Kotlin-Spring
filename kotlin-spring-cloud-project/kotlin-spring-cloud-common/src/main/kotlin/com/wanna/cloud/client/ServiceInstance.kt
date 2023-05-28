package com.wanna.cloud.client

import com.wanna.framework.lang.Nullable

/**
 * 这是SpringCloud对于一个微服务的实例去进行的一层抽象
 *
 * @see com.wanna.cloud.client.serviceregistry.ServiceRegistry
 */
interface ServiceInstance {

    /**
     * 获取SpringCloud微服务的名称, serviceName
     *
     * @return 当前实例对应的微服务名称serviceId
     */
    fun getServiceId(): String

    /**
     * 获取在微服务当中, 当前实例的实例ID
     *
     * @return 当前实例的实例ID
     */
    @Nullable
    fun getInstanceId(): String? = null

    /**
     * 获取当前实例的Host
     *
     * @return 当前实例host
     */
    fun getHost(): String

    /**
     * 获取当前实例所暴露的端口号
     *
     * @return 当前实例所暴露的端口号
     */
    fun getPort(): Int

    /**
     * 服务注册时, 应该使用http/https去进行注册
     *
     * @return true代表应该使用https去进行注册, false代表应该使用http去进行注册
     */
    fun isSecure(): Boolean

    /**
     * 获取当前实例的URI
     *
     * @return 当前实例暴露的uri
     */
    fun getUri(): String

    /**
     * 获取当前实例的元信息
     *
     * @return 实例的元信息Metadata
     */
    fun getMetadata(): Map<String, String>

    /**
     * 获取服务暴露的协议类型(比如http, https)
     *
     * @return 服务暴露的协议类型
     */
    @Nullable
    fun getSchema(): String? = null;
}