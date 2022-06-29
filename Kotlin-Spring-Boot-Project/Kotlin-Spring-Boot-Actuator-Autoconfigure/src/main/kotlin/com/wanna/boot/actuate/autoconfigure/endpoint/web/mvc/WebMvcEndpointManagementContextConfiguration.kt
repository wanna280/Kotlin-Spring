package com.wanna.boot.actuate.autoconfigure.endpoint.web.mvc

import com.wanna.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties
import com.wanna.boot.actuate.endpoint.ExposableEndpoint
import com.wanna.boot.actuate.endpoint.annotation.WebEndpointDiscoverer
import com.wanna.boot.actuate.endpoint.web.EndpointMapping
import com.wanna.boot.actuate.endpoint.web.ExposableWebEndpoint
import com.wanna.boot.actuate.endpoint.web.WebEndpointsSupplier
import com.wanna.boot.actuate.endpoint.web.mvc.WebMvcEndpointHandlerMapping
import com.wanna.boot.context.properties.EnableConfigurationProperties
import com.wanna.framework.context.ApplicationContext
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

@EnableConfigurationProperties([WebEndpointProperties::class])
@Configuration(proxyBeanMethods = false)
open class WebMvcEndpointManagementContextConfiguration {
    @Bean
    open fun webMvcEndpointHandlerMapping(
        properties: WebEndpointProperties,
        webEndpointsSupplier: WebEndpointsSupplier
    ): WebMvcEndpointHandlerMapping {
        val webEndpoints = ArrayList<ExposableWebEndpoint>()
        webEndpoints.addAll(webEndpointsSupplier.getEndpoints())
        val endpointMapping = EndpointMapping(properties.basePath)
        return WebMvcEndpointHandlerMapping(webEndpoints, endpointMapping)
    }

    @Bean
    open fun discoverer(applicationContext: ApplicationContext): WebEndpointDiscoverer {
        return WebEndpointDiscoverer(applicationContext)
    }
}