package com.wanna.cloud.openfeign

import com.wanna.cloud.context.named.NamedContextFactory

/**
 * Feign对于NamedContextFactory.Specification的实现
 *
 * @param name childContextName(serviceName)
 * @param configurations childContext当中的配置类列表
 */
open class FeignClientSpecification(private val name: String, private val configurations: Array<Class<*>>) :
    NamedContextFactory.Specification {

    override fun getName() = this.name

    override fun getConfigurations() = this.configurations
}