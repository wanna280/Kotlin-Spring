package com.wanna.cloud.netflix.ribbon

import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.annotation.Import
import kotlin.reflect.KClass

/**
 * 标识这是一个RibbonClient
 *
 * @param value clientName
 * @param name clientName
 * @param configurations RibbonClient的配置类列表
 */
@Import([RibbonClientConfigurationRegistrar::class])
@Configuration(proxyBeanMethods = false)
annotation class RibbonClient(val value: String = "", val name: String = "", val configurations: Array<KClass<*>>)
