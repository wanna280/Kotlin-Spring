package com.wanna.boot.actuate.autoconfigure.endpoint.web

import com.wanna.boot.actuate.endpoint.annotation.WebEndpointDiscoverer
import com.wanna.boot.actuate.endpoint.web.annotation.ControllerEndpointDiscoverer
import com.wanna.boot.context.properties.EnableConfigurationProperties
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

/**
 * WebEndpoint的自动配置类
 *
 * * 1.自动装配[WebEndpointDiscoverer], 提供`@Endpoint`注解标注的类上的`Operation`方法的解析功能
 * * 2.自动装配[ControllerEndpointDiscoverer], 提供`@ControllerEndpoint`注解标注的类上的`@RequestMapping`方法的解析功能
 */
@EnableConfigurationProperties([WebEndpointProperties::class])
@Configuration(proxyBeanMethods = false)
open class WebEndpointAutoConfiguration {
    /**
     * 提供Web的Endpoint的Supplier, 负责扫描所有的`@Endpoint`注解当中的Operation方法作为Handler方法
     *
     * @param applicationContext 要去暴露Endpoint的ApplicationContext
     * @return WebEndpointDiscoverer
     */
    @Bean
    open fun webEndpointDiscoverer(applicationContext: ApplicationContext): WebEndpointDiscoverer {
        return WebEndpointDiscoverer(applicationContext)
    }

    /**
     * 提供ControllerEndpoint的Supplier, 负责扫描所有的`@ControllerEndpoint`当中的`@RequestMapping`方法作为Handler方法
     *
     * @param applicationContext 要去暴露Endpoint的ApplicationContext
     * @return ControllerEndpointDiscoverer
     */
    @Bean
    open fun controllerEndpointDiscoverer(applicationContext: ApplicationContext): ControllerEndpointDiscoverer {
        return ControllerEndpointDiscoverer(applicationContext)
    }
}