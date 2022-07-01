package com.wanna.boot.actuate.autoconfigure.endpoint.web.mvc

import com.wanna.boot.actuate.autoconfigure.endpoint.web.WebEndpointAutoConfiguration
import com.wanna.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties
import com.wanna.boot.actuate.endpoint.web.EndpointLinksResolver
import com.wanna.boot.actuate.endpoint.web.EndpointMapping
import com.wanna.boot.actuate.endpoint.web.ExposableWebEndpoint
import com.wanna.boot.actuate.endpoint.web.WebEndpointsSupplier
import com.wanna.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier
import com.wanna.boot.actuate.endpoint.web.mvc.ControllerEndpointHandlerMapping
import com.wanna.boot.actuate.endpoint.web.mvc.WebMvcEndpointHandlerMapping
import com.wanna.boot.autoconfigure.AutoConfigureAfter
import com.wanna.boot.autoconfigure.condition.ConditionalOnMissingBean
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

/**
 * WebMvc的Endpoint的自动配置类
 */
@AutoConfigureAfter([WebEndpointAutoConfiguration::class])
@Configuration(proxyBeanMethods = false)
open class WebMvcEndpointManagementContextConfiguration {

    /**
     * WebMvc的Endpoint的HandlerMapping，负责提供Endpoint的请求的处理
     *
     * @param properties Endpoint的配置信息
     * @param webEndpointsSupplier WebEndpoint的Supplier
     * @return 需要导入到SpringBeanFactory的HandlerMapping
     */
    @Bean
    @ConditionalOnMissingBean
    open fun webMvcEndpointHandlerMapping(
        properties: WebEndpointProperties,
        webEndpointsSupplier: WebEndpointsSupplier
    ): WebMvcEndpointHandlerMapping {
        val webEndpoints = ArrayList<ExposableWebEndpoint>()

        // 获取WebEndpointSupplier提供的Endpoint列表
        webEndpoints.addAll(webEndpointsSupplier.getEndpoints())
        val endpointMapping = EndpointMapping(properties.basePath)

        return WebMvcEndpointHandlerMapping(
            webEndpoints,
            endpointMapping,
            properties.discovery.enabled,
            EndpointLinksResolver(webEndpoints)
        )
    }

    /**
     * Controller的Endpoint的HandlerMapping，支持使用Controller的方式去进行配置Endpoint
     *
     * @param controllerEndpointsSupplier ControllerEndpoint的Supplier
     * @param properties WebEndpointProperties
     * @return ControllerEndpointHandlerMapping
     */
    @Bean
    @ConditionalOnMissingBean
    open fun controllerEndpointHandlerMapping(
        controllerEndpointsSupplier: ControllerEndpointsSupplier,
        properties: WebEndpointProperties
    ): ControllerEndpointHandlerMapping {
        return ControllerEndpointHandlerMapping(
            controllerEndpointsSupplier.getEndpoints(),
            EndpointMapping(properties.basePath)
        )
    }


}