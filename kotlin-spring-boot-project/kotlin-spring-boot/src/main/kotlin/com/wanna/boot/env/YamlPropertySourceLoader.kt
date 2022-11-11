package com.wanna.boot.env

import com.wanna.framework.core.environment.PropertySource
import com.wanna.framework.core.io.Resource

/**
 * 这是一个Yaml的PropertySourceLoader
 *
 * @see PropertySourceLoader
 */
open class YamlPropertySourceLoader : PropertySourceLoader {
    override fun getFileExtensions() = arrayOf("yaml", "yml")

    // TODO
    override fun load(name: String, resource: String): List<PropertySource<*>> {
        return emptyList()
    }

    // TODO
    override fun load(name: String, resource: Resource): List<PropertySource<*>> {
        return emptyList()
    }
}