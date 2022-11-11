package com.wanna.cloud.netflix.ribbon

import com.wanna.framework.context.annotation.Configuration
import com.wanna.framework.context.annotation.Import
import kotlin.reflect.KClass

/**
 * 组合了多个RibbonClient
 *
 * @param value 要组合的RibbonClient列表
 * @param defaultConfiguration RibbonClient的默认配置类
 */
@Import([RibbonClientConfigurationRegistrar::class])
@Configuration(proxyBeanMethods = false)
annotation class RibbonClients(val value: Array<RibbonClient> = [], val defaultConfiguration: Array<KClass<*>>)
