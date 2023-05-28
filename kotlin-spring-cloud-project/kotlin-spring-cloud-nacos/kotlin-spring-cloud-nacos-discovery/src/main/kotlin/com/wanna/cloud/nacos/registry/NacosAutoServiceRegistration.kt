package com.wanna.cloud.nacos.registry

import com.wanna.cloud.client.serviceregistry.AbstractAutoServiceRegistration
import com.wanna.cloud.client.serviceregistry.AutoServiceRegistrationProperties
import com.wanna.cloud.client.serviceregistry.Registration

/**
 * 去完成SpringCloud当中的自动注册的NacosAutoServiceRegistration
 *
 * @param registry 服务的注册中心
 * @param properties 服务自动注册的相关信息
 * @param registration Nacos服务的相关配置信息
 */
open class NacosAutoServiceRegistration(
    registry: NacosServiceRegistry,
    properties: AutoServiceRegistrationProperties,
    private val registration: NacosRegistration
) : AbstractAutoServiceRegistration<Registration>(registry, properties) {

    /**
     * Nacos服务注册的实例信息Registration
     *
     * @return Nacos服务注册的实例信息Registration
     */
    override fun getRegistration(): Registration = registration

    /**
     * Nacos服务注册的相关配置信息
     *
     * @return Nacos服务注册的相关信息
     */
    override fun getConfiguration(): Any = registration.properties
}