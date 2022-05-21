package com.wanna.cloud.netflix.ribbon

import com.wanna.cloud.context.named.NamedContextFactory

/**
 * Ribbon针对NamedContextFactory.Specification的实现
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