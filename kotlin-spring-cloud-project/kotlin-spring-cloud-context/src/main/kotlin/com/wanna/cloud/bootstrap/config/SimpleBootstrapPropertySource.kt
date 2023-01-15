package com.wanna.cloud.bootstrap.config

import com.wanna.framework.core.environment.PropertySource

/**
 * 这是一个简单的Bootstrap的PropertySource, 提供了对一个PropertySource的委托的包装, 用来标识它是一个Bootstrap的PropertySource
 *
 * @param propertySource 要进行包装的PropertySource
 */
open class SimpleBootstrapPropertySource<T>(private val propertySource: PropertySource<T>) :
    PropertySource<T>(propertySource.name, propertySource.source) {

    override fun getProperty(name: String): Any? {
        return propertySource.getProperty(name)
    }
}