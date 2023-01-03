package com.wanna.boot.context.properties

import com.wanna.framework.core.annotation.AliasFor
import kotlin.reflect.KClass

/**
 * ConfigurationPropertiesScan，扫描指定的包下的所有的ConfigurationProperties
 */
@EnableConfigurationProperties
annotation class ConfigurationPropertiesScan(
    @get:AliasFor("value")
    val basePackages: Array<String> = [],
    @get:AliasFor("value")
    val value: Array<String> = [],
    val basePackageNames: Array<KClass<*>> = []
)
