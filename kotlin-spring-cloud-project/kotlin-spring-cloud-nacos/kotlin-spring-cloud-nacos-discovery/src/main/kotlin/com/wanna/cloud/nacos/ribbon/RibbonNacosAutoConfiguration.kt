package com.wanna.cloud.nacos.ribbon

import com.wanna.boot.autoconfigure.AutoConfigureAfter
import com.wanna.boot.autoconfigure.condition.ConditionalOnBean
import com.wanna.cloud.netflix.ribbon.RibbonAutoConfiguration
import com.wanna.cloud.netflix.ribbon.RibbonClients
import com.wanna.cloud.netflix.ribbon.SpringClientFactory
import com.wanna.framework.context.annotation.Configuration

/**
 * 标识这是一个RibbonClient, 去配置默认的RibbonClient的配置
 */
@ConditionalOnBean(value = [SpringClientFactory::class])
@AutoConfigureAfter(value = [RibbonAutoConfiguration::class])
@RibbonClients(defaultConfiguration = [NacosRibbonClientConfiguration::class])
@Configuration(proxyBeanMethods = false)
open class RibbonNacosAutoConfiguration