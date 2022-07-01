package com.wanna.framework.core.io.support

import com.wanna.framework.core.environment.PropertiesPropertySource
import com.wanna.framework.core.environment.PropertySource

/**
 * 这是一个PropertySourceFactory的默认实现
 *
 * @see PropertiesLoaderUtils
 * @see PropertySourceFactory
 * @see PropertySource
 */
open class DefaultPropertySourceFactory : PropertySourceFactory {
    companion object {
        private const val DEFAULT_PROPERTY_SOURCE_NAME = "DEFAULT"
    }

    override fun createPropertySource(name: String?, resource: String): PropertySource<*> {
        val properties = PropertiesLoaderUtils.loadProperties(resource)
        return PropertiesPropertySource(name ?: DEFAULT_PROPERTY_SOURCE_NAME, properties)
    }
}