package com.wanna.framework.context.annotation

interface PropertySourceFactory {
    fun create(name: String, resources: Array<String>): com.wanna.framework.core.environment.PropertySource<*>
}