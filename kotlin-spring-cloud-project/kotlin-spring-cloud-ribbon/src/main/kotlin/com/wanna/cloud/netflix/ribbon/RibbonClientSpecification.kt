package com.wanna.cloud.netflix.ribbon

import com.wanna.cloud.context.named.NamedContextFactory

/**
 * Ribbon针对NamedContextFactory.Specification的实现, 为某个特定的childContext当中去提供默认的配置类
 *
 * @param name clientName(serviceName/childContextName)
 * @param configurations childContext当中应该apply的配置类列表
 */
open class RibbonClientSpecification(private val name: String, private val configurations: Array<Class<*>>) :
    NamedContextFactory.Specification {

    override fun getName(): String {
        return this.name
    }

    override fun getConfigurations(): Array<Class<*>> {
        return this.configurations
    }
}