package com.wanna.cloud.nacos.registry

import com.wanna.cloud.client.serviceregistry.AbstractAutoServiceRegistration
import com.wanna.cloud.client.serviceregistry.AutoServiceRegistrationProperties
import com.wanna.cloud.client.serviceregistry.Registration

/**
 * 去完成SpringCloud当中的自动注册的NacosAutoServiceRegistration
 */
open class NacosAutoServiceRegistration(
    registry: NacosServiceRegistry,
    properties: AutoServiceRegistrationProperties,
    private val registration: Registration
) :
    AbstractAutoServiceRegistration<Registration>(registry, properties) {

    override fun getRegistration(): Registration {
        return registration
    }
}