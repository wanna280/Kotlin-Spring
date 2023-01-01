package com.wanna.boot.context.properties

import org.springframework.core.annotation.AliasFor
import kotlin.reflect.KClass

/**
 * ConfigurationPropertiesScan，扫描指定的包下的所有的ConfigurationProperties
 */
@EnableConfigurationProperties
annotation class ConfigurationPropertiesScan(
    @get:com.wanna.framework.core.annotation.AliasFor("value")
    @get:AliasFor("value")
    val basePackages: Array<String> = [],
    @get:com.wanna.framework.core.annotation.AliasFor("value")
    @get:AliasFor("basePackages")
    val value: Array<String> = [],
    val basePackageNames: Array<KClass<*>> = []
)
