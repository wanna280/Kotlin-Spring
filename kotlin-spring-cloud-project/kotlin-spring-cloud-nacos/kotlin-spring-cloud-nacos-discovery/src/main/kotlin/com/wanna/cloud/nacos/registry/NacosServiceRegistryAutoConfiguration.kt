package com.wanna.cloud.nacos.registry

import com.wanna.boot.autoconfigure.AutoConfigureAfter
import com.wanna.boot.autoconfigure.condition.ConditionalOnBean
import com.wanna.boot.context.properties.EnableConfigurationProperties
import com.wanna.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration
import com.wanna.cloud.client.serviceregistry.AutoServiceRegistrationProperties
import com.wanna.cloud.nacos.NacosDiscoveryProperties
import com.wanna.cloud.nacos.NacosServiceManager
import com.wanna.cloud.nacos.discovery.NacosDiscoveryAutoConfiguration
import com.wanna.framework.context.annotation.Autowired
import com.wanna.framework.context.annotation.Bean
import com.wanna.framework.context.annotation.Configuration

/**
 * 它是一个Nacos的服务注册的自动配置类, 负责给容器当中去导入自动配置类, 去提供SpringCloud的服务自动注册;
 * 这个自动配置类, 需要交给Spring容器去进行管理, 让Spring在WebSever已经准备好的时候, 使用事件发布机制;
 * 可以去自动回调NacosServiceRegistry(Nacos的服务注册中心), 从而实现NacosRegistration实例的自动注册
 */
@AutoConfigureAfter(value = [NacosDiscoveryAutoConfiguration::class, AutoServiceRegistrationConfiguration::class])
@EnableConfigurationProperties
@Configuration(proxyBeanMethods = false)
open class NacosServiceRegistryAutoConfiguration {

    /**
     * Nacos的服务注册中心, 通过组合NacosServiceManager和NacosDiscoveryProperties去完成服务的注册
     *
     * @param properties NacosDiscoveryProperties
     * @param manager NacosServiceManager
     */
    @Bean
    open fun nacosServiceRegistry(
        properties: NacosDiscoveryProperties, manager: NacosServiceManager
    ): NacosServiceRegistry {
        return NacosServiceRegistry(manager, properties)
    }

    /**
     * 给容器当中去注册一个自动注册的实例(NacosRegistration), 组合到NacosAutoServiceRegistration当中;
     * 被NacosAutoServiceRegistration监听到WebServer初始化完成事件时, 被Spring去完成自动注册
     *
     * @param customizers NacosRegistration的自定义化器, 支持去对NacosRegistration去进行自定义(可以不配置)
     * @param properties NacosDiscoveryProperties, Nacos服务注册和发现的配置信息
     */
    @Bean
    @ConditionalOnBean([AutoServiceRegistrationProperties::class])
    open fun nacosRegistration(
        @Autowired(required = false) customizers: List<NacosRegistrationCustomizer>,
        properties: NacosDiscoveryProperties
    ): NacosRegistration {
        return NacosRegistration(customizers, properties)
    }

    /**
     * 给容器中注册一个NacosAutoServiceRegistration, 去完成服务的自动注册和发现, 配合SpringCloud的去完成
     *
     * @param properties AutoServiceRegistrationProperties, 标识SpringCloudCommon包存在
     * @param registry Nacos的实例注册中心
     * @param registration NacosRegistration(要进行自动服务注册的实例)
     */
    @Bean
    @ConditionalOnBean([AutoServiceRegistrationProperties::class])
    open fun nacosAutoServiceRegistration(
        properties: AutoServiceRegistrationProperties, registry: NacosServiceRegistry, registration: NacosRegistration
    ): NacosAutoServiceRegistration {
        return NacosAutoServiceRegistration(registry, properties, registration)
    }
}