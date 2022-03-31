package com.wanna.framework.context.annotations

interface PropertySourceFactory {
    fun create(name: String, resources: Array<String>): com.wanna.framework.core.environment.PropertySource
}