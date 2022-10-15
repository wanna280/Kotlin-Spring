package com.wanna.framework.core.io.support

import com.wanna.framework.core.environment.PropertiesPropertySource
import com.wanna.framework.core.environment.PropertySource
import com.wanna.framework.core.io.Resource
import com.wanna.framework.lang.Nullable
import java.util.*

/**
 * 这是一个PropertySourceFactory的默认实现
 *
 * @see PropertiesLoaderUtils
 * @see PropertySourceFactory
 * @see PropertySource
 */
open class DefaultPropertySourceFactory : PropertySourceFactory {
    companion object {
        /**
         * 默认的PropertySource的name
         */
        private const val DEFAULT_PROPERTY_SOURCE_NAME = "DEFAULT"
    }

    override fun createPropertySource(@Nullable name: String?, resource: String): PropertySource<*> {
        return createPropertySource(name, PropertiesLoaderUtils.loadProperties(resource))
    }

    override fun createPropertySource(@Nullable name: String?, resource: Resource): PropertySource<*> {
        return createPropertySource(name, PropertiesLoaderUtils.loadProperties(resource))
    }

    /**
     * 创建一个PropertySource
     *
     * @param name name(可以为空，为空时，将会使用默认的name)
     * @param properties 加载到的Properties
     * @return 构建出来的PropertySource
     */
    private fun createPropertySource(@Nullable name: String?, properties: Properties): PropertySource<*> {
        return PropertiesPropertySource(name ?: DEFAULT_PROPERTY_SOURCE_NAME, properties)
    }
}