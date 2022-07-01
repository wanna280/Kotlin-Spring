package com.wanna.boot.actuate.autoconfigure.endpoint.web

import com.wanna.boot.actuate.endpoint.annotation.WebEndpointDiscoverer
import com.wanna.boot.actuate.endpoint.web.annotation.ControllerEndpointDiscoverer
import com.wanna.boot.context.properties.EnableConfigurationProperties
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

/**
 * WebEndpoint的自动配置类
 */
@EnableConfigurationProperties([WebEndpointProperties::class])
@Configuration(proxyBeanMethods = false)
open class WebEndpointAutoConfiguration {
    /**
     * 提供Web的Endpoint的Supplier，负责扫描所有的@Endpoint注解当中的Operation方法作为Handler方法
     *
     * @param applicationContext 要去暴露Endpoint的ApplicationContext
     * @return WebEndpointDiscoverer
     */
    @Bean
    open fun webEndpointDiscoverer(applicationContext: ApplicationContext): WebEndpointDiscoverer {
        return WebEndpointDiscoverer(applicationContext)
    }

    /**
     * 提供ControllerEndpoint的Supplier，负责扫描所有的ControllerEndpoint当中的@RequestMapping方法作为Handler方法
     *
     * @param applicationContext 要去暴露Endpoint的ApplicationContext
     * @return ControllerEndpointDiscoverer
     */
    @Bean
    open fun controllerEndpointDiscoverer(applicationContext: ApplicationContext): ControllerEndpointDiscoverer {
        return ControllerEndpointDiscoverer(applicationContext)
    }
}