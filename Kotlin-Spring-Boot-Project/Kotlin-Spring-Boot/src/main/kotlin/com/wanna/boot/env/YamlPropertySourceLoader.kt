package com.wanna.boot.env

import com.wanna.framework.core.environment.PropertySource

/**
 * 这是一个Yaml的PropertySourceLoader
 */
open class YamlPropertySourceLoader : PropertySourceLoader {
    override fun getFileExtensions(): Array<String> {
        return arrayOf("yaml", "yml")
    }

    override fun load(name: String, resource: String): List<PropertySource<*>> {
        return emptyList()
    }
}