package com.wanna.cloud.bootstrap.config

import com.wanna.framework.core.environment.EnumerablePropertySource

/**
 * 这是一个对EnumerablePropertySource的包装, 主要用于标识它是一个Bootstrap的PropertySource
 *
 * @param delegate 要委托的EnumerablePropertySource
 */
open class BootstrapPropertySource<T>(private val delegate: EnumerablePropertySource<T>) :
    EnumerablePropertySource<T>("bootstrapProperties-${delegate.name}", delegate.source) {

    override fun getPropertyNames(): Array<String> {
        return delegate.getPropertyNames()
    }

    override fun getProperty(name: String): Any? {
        return delegate.getProperty(name)
    }
}